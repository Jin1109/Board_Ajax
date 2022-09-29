drop table member cascade constraints purge;
--1. index.jsp에서 시작 합니다.
--2. 관리자 계정 admin, 비번 1234를 만듭니다.
--3. 사용자 계정을 3개 만듭니다.

create table member(
	id			varchar2(12),
	password	varchar2(10),
	name		varchar2(15),
	age			number(2),
	gender		varchar2(3),
	email		varchar2(30),
	memberfile	VARCHAR2(50),
	PRIMARY	KEY(id)
);

select * from MEMBER;

delete from member where id = 'test11';
delete from member where id = 'test1';
delete from member where id = 'test2';
delete from member where id = 'test4';
delete from member where id = 'test5';
delete from member where id = 'test7';
delete from member where id = 'test9';

--memberfile은 회원 정보 수정시 적용합니다.