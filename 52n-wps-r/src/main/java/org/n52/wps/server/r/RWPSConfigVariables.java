
package org.n52.wps.server.r;

public enum RWPSConfigVariables {

    SCRIPT_DIR, RESOURCE_DIR, ALGORITHM, ENABLE_BATCH_START, RSERVE_HOST, RSERVE_PORT, RSERVE_USER, RSERVE_PASSWORD;

    public String toString() {
        switch (this) {
        case SCRIPT_DIR:
            return "Script_Dir";
        case RESOURCE_DIR:
        	return "Resource_Dir";
        case ALGORITHM:
            return "Algorithm";
        case ENABLE_BATCH_START:
            return "Enable_Batch_Start";
        case RSERVE_HOST:
            return "Rserve_Host";
        case RSERVE_PORT:
            return "Rserve_Port";
        case RSERVE_USER:
            return "Rserve_User";
        case RSERVE_PASSWORD:
            return "Rserve_Password";
        default:
            return "NO STRING REPRESENTATION DEFINED FOR ENUM CONSTANT!";
        }

    };

}
