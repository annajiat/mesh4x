DROP TABLE mesh_sync_info;

CREATE TABLE mesh_sync_info (
  sync_id varchar(100) PRIMARY KEY not null,
  entity_name varchar(100) not null,
  entity_id varchar(100) not null,
  entity_version int not null,
  sync_data LONGTEXT);
  
  
DROP TABLE mesh_sync_example;

CREATE TABLE mesh_sync_example (
  uid varchar(100) PRIMARY KEY not null,
  name varchar(100) not null,
  pass varchar(100) not null);

  
DROP TABLE mesh_example_1;

CREATE TABLE mesh_example_1 (
  uid varchar(100) PRIMARY KEY not null,
  vString varchar(100) not null,
  vdecimal decimal(5,3) not null  ,
  vdouble double(5,3) not null,
  vfloat float(5,3) not null  
);


insert into mesh_example_1 (uid, vString, vdecimal, vdouble, vfloat) values('2', 'jmt', 3.257, 3.257, 3.257)
insert into mesh_example_1 (uid, vString, vdecimal, vdouble, vfloat) values('3', 'jmt2', 3, 3, 3)

