<?xml version="1.0" encoding="UTF-8"?>
<!-- Example mapping file for ArcToolbox algorithms Author: Matthias Mueller, 
	TU Dresden -->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ows="http://www.opengis.net/ows/1.1"
	xmlns:wps="http://www.opengis.net/wps/1.0.0" xmlns:xs="http://www.w3.org/2001/XMLSchema"
	exclude-result-prefixes="ows wps xs">
	<xsl:output method="xml" encoding="UTF-8" indent="yes" />
	<xsl:template match="/">
		<xsl:variable name="docRoot" select="." />
		<AlgorithmDescription>
			<xsl:attribute name="xsi:tns"
				namespace="http://www.w3.org/2001/XMLSchema-instance">
				<xsl:value-of
				select="'http://141.30.100.159/feed/AlgorithmDescription.xsd'" />
			</xsl:attribute>

			<!-- General parameters -->
			<workspaceLocation>./org.n52.wps.example.ags.custom.buffer</workspaceLocation>
			<algorithmLocation>algorithm://org.n52.wps.example.ags.custom.buffer/buffertoolbox.tbx?custombuffername</algorithmLocation>
			<containerType>urn:n52:esri:arctoolbox:10.0</containerType>
			<requiredRuntimeComponent>urn:n52:esri:arcgis:10.0</requiredRuntimeComponent>
			<algorithmParameters sequential="true">

				<!-- Inputs -->
				<parameter>
					<prefixString />
					<suffixString />
					<separatorString />
					<positionID>0</positionID>
					<wpsInputID>inputfeatures</wpsInputID>
					<wpsOutputID />
				</parameter>
				
				<parameter>
					<prefixString />
					<suffixString />
					<separatorString />
					<positionID>2</positionID>
					<wpsInputID>distance</wpsInputID>
					<wpsOutputID />
				</parameter>


				<!-- Outputs -->

				<parameter>
					<prefixString />
					<suffixString />
					<separatorString />
					<positionID>1</positionID>
					<wpsInputID />
					<wpsOutputID>buffers</wpsOutputID>
				</parameter>

			</algorithmParameters>
		</AlgorithmDescription>
	</xsl:template>
</xsl:stylesheet>
