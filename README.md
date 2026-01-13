# Radius-2FA

PROFILE=prod

# Application Settings
APP_NAME=Radius-2FA
APP_VERSION=1.0.0
LOG_LEVEL=INFO

LDAP_URL=ldap://localhost:389
LDAP_BASE=ou=CorpUsers,dc=corp,dc=domain,dc=com
LDAP_USERNAME=CORP\\LDAP
LDAP_PASSWORD=password

JDBC_URL=jdbc:h2:file:./databases/database;AUTO_RECONNECT=TRUE;MODE=PostgreSQL;
JDBC_USERNAME=sa
JDBC_PASSWORD=password
JDBC_DRIVER=org.h2.Driver
DB_SHOW_SQL=true
DB_DDL_AUTO=update

MAIL_HOST=smtp.mail.com
MAIL_PORT=587
MAIL_USERNAME=noreply@domain.com
MAIL_PASSWORD=password
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS_ENABLE=true

TOTP_ISSUER=Organization
TOTP_NTP=0.pool.ntp.org

RADIUS_SECRET=SECRETKEY
RADIUS_GROUP=CN=VPN,OU=CorpGroups,DC=corp,DC=domain,DC=com

GEOIP_DATABASE_PATH=/databases/GeoLite2-City.mmdb
GEOIP_DATABASE_URL=https://cdn.jsdelivr.net/npm/geolite2-city/GeoLite2-City.mmdb.gz
GEOIP_DATABASE_UPDATE_CRON=0 0 2 * * *
GEOIP_DATABASE_UPDATE_ENABLED=true