-- Jalankan pada project Supabase setelah meninjau SQL ini.
-- Tidak berisi credential. Migrasi bersifat transaksional dan tidak menghapus data lokal.
begin;

create table public.profiles (
    id uuid primary key references auth.users(id) on delete cascade,
    full_name text not null check (char_length(btrim(full_name)) between 1 and 100),
    role text not null default 'BUYER' check (role in ('ADMIN', 'BUYER')),
    created_at timestamptz not null default now()
);

alter table public.profiles enable row level security;

create function public.is_admin()
returns boolean language sql stable security definer set search_path = ''
as $$
    select exists (
        select 1 from public.profiles where id = (select auth.uid()) and role = 'ADMIN'
    );
$$;
revoke all on function public.is_admin() from public, anon;
grant execute on function public.is_admin() to authenticated;

create function public.handle_new_user()
returns trigger language plpgsql security definer set search_path = ''
as $$
begin
    insert into public.profiles(id, full_name, role)
    values (
        new.id,
        left(coalesce(nullif(btrim(new.raw_user_meta_data ->> 'full_name'), ''), 'Pengguna'), 100),
        'BUYER'
    );
    return new;
end;
$$;
revoke all on function public.handle_new_user() from public, anon, authenticated;
create trigger on_auth_user_created
after insert on auth.users for each row execute procedure public.handle_new_user();

-- Akun lama memperoleh profil BUYER; promosi ADMIN dilakukan secara administratif.
insert into public.profiles(id, full_name)
select id, left(coalesce(nullif(btrim(raw_user_meta_data ->> 'full_name'), ''), 'Pengguna'), 100)
from auth.users
on conflict (id) do nothing;

revoke all on public.profiles from anon, authenticated;
grant select on public.profiles to authenticated;
grant update(full_name) on public.profiles to authenticated;
create policy profiles_read on public.profiles for select to authenticated
using (id = (select auth.uid()) or (select public.is_admin()));
create policy profiles_update_own on public.profiles for update to authenticated
using (id = (select auth.uid())) with check (id = (select auth.uid()));
-- Tidak ada INSERT/DELETE klien; role tidak termasuk kolom UPDATE yang diizinkan.

create table public.categories (
    id uuid primary key default gen_random_uuid(),
    name text not null check (
        char_length(btrim(name)) between 1 and 40
        and name = btrim(regexp_replace(name, '\s+', ' ', 'g'))
        and lower(name) <> 'semua'
    ),
    catalog_locked boolean not null default false,
    created_at timestamptz not null default now()
);
create unique index categories_name_unique on public.categories(lower(name));
alter table public.categories enable row level security;

-- Mengikuti kategori katalog yang saat ini masih berada di DummyProductRepository.
-- Kolom catalog_locked hanya boleh dikelola backend.
insert into public.categories(name, catalog_locked)
values ('Makanan', true), ('Camilan', true), ('Manisan', true);

revoke all on public.categories from anon, authenticated;
grant select on public.categories to authenticated;
grant insert(name) on public.categories to authenticated;
grant update(name) on public.categories to authenticated;
grant delete on public.categories to authenticated;

create policy categories_read on public.categories for select to authenticated using (true);
create policy categories_create_admin on public.categories for insert to authenticated
with check ((select public.is_admin()) and not catalog_locked);
create policy categories_update_admin on public.categories for update to authenticated
using ((select public.is_admin()) and not catalog_locked)
with check ((select public.is_admin()) and not catalog_locked);
create policy categories_delete_admin on public.categories for delete to authenticated
using ((select public.is_admin()) and not catalog_locked);

create function public.admin_overview()
returns jsonb language plpgsql stable security invoker set search_path = ''
as $$
begin
    if not public.is_admin() then
        raise exception 'Admin access required' using errcode = '42501';
    end if;
    return jsonb_build_object(
        'category_count', (select count(*) from public.categories),
        'buyer_count', (select count(*) from public.profiles where role = 'BUYER')
    );
end;
$$;
revoke all on function public.admin_overview() from public, anon;
grant execute on function public.admin_overview() to authenticated;

commit;

-- ADMIN: buat akun melalui Auth terlebih dahulu, lalu jalankan secara manual
-- dari SQL Editor/backend tepercaya menggunakan UUID akun yang sudah diverifikasi:
-- update public.profiles set role = 'ADMIN' where id = '<UUID akun admin>';
-- Jangan membuat endpoint publik untuk mengubah role.
--
-- Konfigurasi Auth: aktifkan Email/Password, tetapkan password minimum >= 8,
-- dan aktifkan Confirm Email. Atur Site URL ke halaman konfirmasi yang valid.
-- Setelah verifikasi melalui email, pengguna kembali ke aplikasi dan login.
--
-- Katalog produk/pesanan belum dimigrasikan oleh file ini. Bila nanti dibuat
-- tabel products, gunakan foreign key kategori dengan ON DELETE RESTRICT
-- dan tegakkan larangan rename kategori terpakai di backend.
