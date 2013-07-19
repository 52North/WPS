CREATE TABLE IF NOT EXISTS configurationentry (
  entry_key varchar(255),
  configuration_module varchar(255),
  configuration_value varchar(255) DEFAULT NULL,
  PRIMARY KEY (entry_key, configuration_module),
);

CREATE TABLE IF NOT EXISTS algorithmentry (
  algorithm_name varchar(255),
  configuration_module varchar(255),
  active boolean DEFAULT true,
  PRIMARY KEY (algorithm_name, configuration_module),
);
