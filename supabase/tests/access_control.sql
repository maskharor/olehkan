-- Jalankan secara terpisah pada project DEVELOPMENT setelah migrasi.
-- Seluruh fixture dibatalkan oleh ROLLBACK. Bukan pengujian yang sudah dieksekusi.
begin;
insert into auth.users(id, email, raw_user_meta_data)
values
('11111111-1111-4111-8111-111111111111', 'rls-buyer@example.test',
 '{"full_name":"Buyer Test","role":"ADMIN"}'::jsonb),
('22222222-2222-4222-8222-222222222222', 'rls-admin@example.test',
 '{"full_name":"Admin Test"}'::jsonb);
update public.profiles set role = 'ADMIN'
where id = '22222222-2222-4222-8222-222222222222';

set local role authenticated;
select set_config('request.jwt.claim.sub', '11111111-1111-4111-8111-111111111111', true);
do $$
declare affected integer;
begin
    if (select role from public.profiles where id = auth.uid()) <> 'BUYER' then
        raise exception 'FAIL: metadata signup menaikkan role';
    end if;
    if exists(select 1 from public.profiles where id = '22222222-2222-4222-8222-222222222222') then
        raise exception 'FAIL: buyer membaca profil lain';
    end if;
    update public.profiles set full_name = 'Buyer Edited' where id = auth.uid();
    get diagnostics affected = row_count;
    if affected <> 1 then raise exception 'FAIL: profil sendiri tidak dapat diedit'; end if;
    update public.profiles set full_name = 'Forbidden'
    where id = '22222222-2222-4222-8222-222222222222';
    get diagnostics affected = row_count;
    if affected <> 0 then raise exception 'FAIL: buyer mengedit profil lain'; end if;
    begin
        update public.profiles set role = 'ADMIN' where id = auth.uid();
        raise exception 'FAIL: role dapat diubah klien';
    exception when insufficient_privilege then null;
    end;
    begin
        insert into public.categories(name) values ('RLS Buyer Forbidden');
        raise exception 'FAIL: buyer menambah kategori';
    exception when insufficient_privilege then null;
    end;
    update public.categories set name = 'Forbidden' where name = 'Makanan';
    get diagnostics affected = row_count;
    if affected <> 0 then raise exception 'FAIL: buyer mengubah kategori'; end if;
    delete from public.categories;
    get diagnostics affected = row_count;
    if affected <> 0 then raise exception 'FAIL: buyer menghapus kategori'; end if;
    begin
        perform public.admin_overview();
        raise exception 'FAIL: buyer mengakses statistik admin';
    exception when insufficient_privilege then null;
    end;
end;
$$;

select set_config('request.jwt.claim.sub', '22222222-2222-4222-8222-222222222222', true);
do $$
declare category_id uuid; affected integer;
begin
    if not public.is_admin() then raise exception 'FAIL: role admin tidak terbaca'; end if;
    insert into public.categories(name) values ('RLS Admin Category') returning id into category_id;
    update public.categories set name = 'RLS Admin Edited' where id = category_id;
    get diagnostics affected = row_count;
    if affected <> 1 then raise exception 'FAIL: admin tidak dapat edit kategori'; end if;
    delete from public.categories where id = category_id;
    get diagnostics affected = row_count;
    if affected <> 1 then raise exception 'FAIL: admin tidak dapat hapus kategori'; end if;
    update public.categories set name = 'Forbidden' where name = 'Makanan';
    get diagnostics affected = row_count;
    if affected <> 0 then raise exception 'FAIL: kategori katalog dapat diubah'; end if;
    delete from public.categories where name = 'Makanan';
    get diagnostics affected = row_count;
    if affected <> 0 then raise exception 'FAIL: kategori katalog dapat dihapus'; end if;
    begin
        update public.categories set catalog_locked = false where name = 'Makanan';
        raise exception 'FAIL: perlindungan katalog dapat dilepas klien';
    exception when insufficient_privilege then null;
    end;
    perform public.admin_overview();
end;
$$;
reset role;
rollback;
