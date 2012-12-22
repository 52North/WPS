package org.n52.wps.commons;

/**
 *
 * @author tkunicki
 */
public class MIMEUtil {

    public static String getSuffixFromMIMEType(String mimeType) {
        String[] mimeTypeSplit = mimeType.split("/");
        String suffix =  mimeTypeSplit[mimeTypeSplit.length - 1];
        if ("geotiff".equalsIgnoreCase(suffix) || "x-geotiff".equalsIgnoreCase(suffix)) {
            suffix = "tiff";
        } else if ("netcdf".equalsIgnoreCase(suffix) || "x-netcdf".equalsIgnoreCase(suffix)) {
            suffix = "nc";
        }
        return suffix;
    }
}
