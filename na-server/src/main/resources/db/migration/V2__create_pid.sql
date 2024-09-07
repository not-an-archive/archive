CREATE TABLE pid (
  uuid    UUID,    -- PID's full int16 value
  ext_msb BIGINT,  -- PID's external most significant int8 value
  ext_lsb BIGINT,  -- PID's external least significant int8 value
  born    INTEGER, -- PID's external born int4 value; digitally = 0x00, physically = 0x01
  copy    INTEGER, -- PID's external copy int4 value; external = 0x00, internal = [0x01 - 0x03]

  PRIMARY KEY (uuid),
  UNIQUE (ext_msb, ext_lsb)
);

CREATE INDEX idx_pid_ext  ON pid (ext_msb, ext_lsb);
CREATE INDEX idx_pid_born ON pid (born);
CREATE INDEX idx_pid_copy ON pid (copy);
