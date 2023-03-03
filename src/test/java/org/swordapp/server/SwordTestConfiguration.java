package org.swordapp.server;

public class SwordTestConfiguration implements SwordConfiguration {
    public boolean returnDepositReceipt() {
        return true;
    }
    
    public boolean returnStackTraceInError() {
        return true;
    }
    
    public boolean returnErrorBody() {
        return true;
    }
    
    public String generator() {
        return "http://www.swordapp.org/";
    }
    
    public String generatorVersion() {
        return "2.0";
    }
    
    public String administratorEmail() {
        return "swordtest@example.org";
    }
    
    public String getAuthType() {
        return "None";
    }
    
    public boolean storeAndCheckBinary() {
        return true;
    }
    
    public String getTempDirectory() {
        return System.getProperty("java.io.tmpdir");
    }
    
    public int getMaxUploadSize() {
        return 1024;
    }
    
    @Override
    public int getMaxUploadFiles() {
        return 1024;
    }
    
    public String getAlternateUrl() {
        return "https://example.org";
    }
    
    public String getAlternateUrlContentType() {
        return "text/html";
    }
    
    public boolean allowUnauthenticatedMediaAccess() {
        return false;
    }
}
