CREATE TABLE IF NOT EXISTS configurationmodule (
  module_class_name varchar(255),
  status boolean,
  PRIMARY KEY (module_class_name)
);

CREATE TABLE IF NOT EXISTS configurationentry (
  entry_key varchar(255),
  configuration_module varchar(255),
  configuration_value varchar(255) DEFAULT NULL,
  PRIMARY KEY (entry_key, configuration_module),
  FOREIGN KEY (configuration_module) REFERENCES configurationmodule (module_class_name)
);

CREATE TABLE IF NOT EXISTS algorithmentry (
  algorithm_name varchar(255),
  configuration_module varchar(255),
  active boolean DEFAULT true,
  PRIMARY KEY (algorithm_name, configuration_module)
);

CREATE TABLE IF NOT EXISTS users (
  user_id int GENERATED BY DEFAULT AS IDENTITY(START WITH 1, INCREMENT BY 1) PRIMARY KEY,
  username varchar(255) NOT NULL UNIQUE,
  password varchar(32) NOT NULL,
  role varchar(255) NOT NULL
);