<?xml version="1.0" encoding="UTF-8"?>
<!--
Mapping file for perimeter.xml

Author: Matthias Mueller, TU Dresden
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ows="http://www.opengis.net/ows/1.1" xmlns:wps="http://www.opengis.net/wps/1.0.0" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="ows wps xs">
	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>
	<xsl:template match="/">
		<xsl:variable name="docRoot" select="."/>
		<GPAlgorithmDescription>
			<xsl:attribute name="xsi:tns" namespace="http://www.w3.org/2001/XMLSchema-instance">
				<xsl:value-of select="'http://52north.org/svn/geoprocessing/main/WPS/trunk/WPS/52n-wps-webapp/src/main/webapp/examples/LegacyAlgorithmSchema.xsd'"/>
			</xsl:attribute>
			
			<!-- General parameters -->
			<algorithmWorkspaceLocation>http://gp-algorithms.tu-dresden.de/de.tu-dresden.wps.perimeter</algorithmWorkspaceLocation>
			<algorithmContainerLocation>http://gp-algorithms.tu-dresden.de/de.tu-dresden.wps.perimeter/toolbox.tbx</algorithmContainerLocation>
			<algorithmLocation>http://gp-algorithms.tu-dresden.de/de.tu-dresden.wps.perimeter/toolbox.tbx?perimeter</algorithmLocation>
			<algorithmContainerURN>urn:n52:esri:arctoolbox:9.3</algorithmContainerURN>
			<processingSystemURN>urn:n52:esri:arcgis:9.3</processingSystemURN>
			
			<algorithmParameters sequential="true">
			
        <!-- Inputs -->	
			  <xsl:for-each select="$docRoot/wps:ProcessDescriptions/ProcessDescription/DataInputs/Input">
				  <xsl:variable name="var2_Input" select="."/>
				  <xsl:variable name="inputItem" select="."/>
				  <xsl:if test="string($inputItem/ows:Identifier) = 'INTEGER_RASTER'">
					  <parameter>
						  <prefixString/>
						  <suffixString/>
						  <separatorString/>
						  <legacyIntID>0</legacyIntID>
						  <wpsInputID><xsl:value-of select="ows:Identifier"/></wpsInputID>
						  <wpsOutputID/>
						  <xsl:call-template name="tInputParam"/>
					  </parameter>
				  </xsl:if>
				  <xsl:if test="string($inputItem/ows:Identifier) = 'SIMPLIFICATION'">
					  <parameter>
						  <prefixString/>
						  <suffixString/>
						  <separatorString/>
						  <legacyIntID>1</legacyIntID>
						  <wpsInputID><xsl:value-of select="ows:Identifier"/></wpsInputID>
						  <wpsOutputID/>
						  <xsl:call-template name="tInputParam"/>
					  </parameter>
				  </xsl:if>
			  </xsl:for-each>
			
			  <!-- Outputs -->	
        <xsl:for-each select="$docRoot/wps:ProcessDescriptions/ProcessDescription/ProcessOutputs/Output">
				  <xsl:variable name="var3_Output" select="."/>
				  <xsl:variable name="outputItem" select="."/>
				  <xsl:if test="string($outputItem/ows:Identifier) = 'PERIMETER'">
					  <parameter>
						  <prefixString/>
						  <suffixString/>
						  <separatorString/>
						  <legacyIntID>2</legacyIntID>
						  <wpsInputID/>
						  <wpsOutputID><xsl:value-of select="ows:Identifier"/></wpsOutputID>
						  <xsl:call-template name="tOutputParam"/>
					  </parameter>
				  </xsl:if>
			  </xsl:for-each>
			  
			</algorithmParameters>
		</GPAlgorithmDescription>
	</xsl:template>
	
	<xsl:template name="tInputParam">
		<wpsDataSchema><xsl:value-of select="ComplexData/Default/Format/Schema"/></wpsDataSchema>
		<wpsMimeType><xsl:value-of select="ComplexData/Default/Format/MimeType"/></wpsMimeType>
		<wpsLiteralDataTye><xsl:value-of select="LiteralData/ows:DataType"/></wpsLiteralDataTye>
		<wpsDefaultCRS><xsl:value-of select="BoundingBoxData/Default/CRS"/></wpsDefaultCRS>
	</xsl:template>
	
	<xsl:template name="tOutputParam">
		<wpsDataSchema><xsl:value-of select="ComplexOutput/Default/Format/Schema"/></wpsDataSchema>
		<wpsMimeType><xsl:value-of select="ComplexOutput/Default/Format/MimeType"/></wpsMimeType>
		<wpsLiteralDataTye><xsl:value-of select="LiteralOutput/ows:DataType"/></wpsLiteralDataTye>
		<wpsDefaultCRS><xsl:value-of select="BoundingBoxOutput/Default/CRS"/></wpsDefaultCRS>
	</xsl:template>	
	
</xsl:stylesheet>
