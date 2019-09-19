use banking;

CREATE TABLE transactions ( id smallint unsigned not null auto_increment,
    payload varchar(1000) not null, constraint pk_example primary key (id) );
