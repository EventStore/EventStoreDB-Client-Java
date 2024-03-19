package com.eventstore.dbclient;

class AuthOptionsBase {
    private UserCredentials userCredentials;
    private UserCertificate userCertificate;

    UserCredentials getUserCredentials() {
        return this.userCredentials;
    }

    UserCertificate getUserCertificate() {
        return this.userCertificate;
    }

    void setUserCredentials(UserCredentials userCredentials) {
        this.userCredentials = userCredentials;
    }

    void setUserCertificate(UserCertificate userCertificate) {
        this.userCertificate = userCertificate;
    }
}
