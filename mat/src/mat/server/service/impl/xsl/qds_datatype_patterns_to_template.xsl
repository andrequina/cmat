<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:exsl="http://exslt.org/common"
   xmlns:uuid="java:java.util.UUID" xmlns:math="http://exslt.org/math" xmlns="urn:hl7-org:v3"
   xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:msxsl="urn:schemas-microsoft-com:xslt"
   extension-element-prefixes="exsl uuid math xs" 
   exclude-result-prefixes="exsl uuid math xs msxsl">
	<xsl:output method="xml" indent="yes" encoding="ISO-8859-1"/>
	
	<xsl:template match="qdsel" mode="to">
		<xsl:variable name="rel_to_uuid">
			<xsl:if test="current()/@to">
				<xsl:value-of select="/measure/elementlookup/*[@id=current()/@to]/@uuid"/>
			</xsl:if>
      	</xsl:variable>
		<xsl:variable name="rel">
	   		<xsl:choose>
	   			<xsl:when test="@rel='SBOD'">EAS</xsl:when>
	   			<xsl:when test="@rel='EBOD'">EAE</xsl:when>
	   			<xsl:otherwise>
	   				<xsl:value-of select="@rel"/>
	   			</xsl:otherwise>
	   		</xsl:choose>
	   	</xsl:variable>
	   	
	   	<xsl:variable name="inversion">
			<xsl:choose>
				<xsl:when test="@rel='SBOD'">true</xsl:when>
				<xsl:when test="@rel='EBOD'">true</xsl:when>
				<xsl:otherwise>false</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:apply-templates select="." mode="resultvalue"/>
		<xsl:choose>
			<xsl:when test="name(..)!='elementlookup' and @rel">
				<sourceOf typeCode="{$rel}">
					<xsl:if test="$inversion = 'true'">
	                  <xsl:attribute name="inversionInd">true</xsl:attribute>                  
					</xsl:if>
					<xsl:attribute name="displayInd">true</xsl:attribute>
					<xsl:if test="@to">
						<xsl:apply-templates select="." mode="pauseQuantity"/>
						<xsl:variable name="to_id">
							<xsl:value-of select="@to"/>
						</xsl:variable>
						<xsl:variable name="to">
							<xsl:choose>
								<xsl:when test="name(/measure/elementlookup/*[@id=$to_id])='iqdsel'">
					      			<xsl:value-of select="/measure/elementlookup/*[@id=$to_id]/@refid"/>
					      		</xsl:when>
					      		<xsl:otherwise>
									<xsl:value-of select="@to"/>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>

						<xsl:variable name="to_title">
							<xsl:call-template name="title">
								<xsl:with-param name="is_to" select="@to"/>
							</xsl:call-template>
						</xsl:variable>
						<observation classCode="OBS" moodCode="EVN" isCriterionInd="true">
							<id root="{$rel_to_uuid}"/>
							<xsl:if test="string-length($to_title)>0">
								<title><xsl:value-of select="$to_title"/></title>
							</xsl:if>
						</observation>
					</xsl:if>
					<xsl:if test="to">
						<xsl:apply-templates select="to/*" mode="to_child"/>
					</xsl:if>
				</sourceOf>
			</xsl:when>
			<xsl:when test="@highnum or @lownum or @equalnum">
				<xsl:apply-templates select="." mode="pauseQuantity"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="*" mode="to_child">
		<xsl:choose>
			<xsl:when test="name(.)='qdsel' or name(.)='measureel' or name(.)='and' or name(.)='or' or name(.)='calc' or name(.)='count'">
				<xsl:apply-templates select="@order" mode="sequence"/>
				<xsl:apply-templates select="../.." mode="pauseQuantity"/>
				<xsl:choose>
					<xsl:when test="reference/qdsel[@id]">
						<xsl:for-each select="reference/qdsel">
							<xsl:apply-templates select="."/>
						</xsl:for-each>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="name(.)='not'">
				<xsl:apply-templates select="." mode="not"/>
			</xsl:when>
			<xsl:when test="name(.)='min' or name(.)='max' or name(.)='last' or name(.)='first' or name(.)='second' or name(.)='third' or name(.)='fourth' or name(.)='fifth'">
				<xsl:apply-templates select="../.." mode="pauseQuantity"/>
				<xsl:apply-templates select="." mode="subset"/>
				<xsl:apply-templates select="./*"/>
			</xsl:when>
			<xsl:when test="name(.)='value'">
				<xsl:apply-templates select=".." mode="resultvalue"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="not" mode="not">
		<xsl:for-each select="./node()">
			<xsl:choose>
				<xsl:when test="name(.)='qdsel' or name(.)='measureel' or name(.)='and' or name(.)='or' or name(.)='calc' or name(.)='count'">
					<xsl:apply-templates select="."/>
				</xsl:when>
				<xsl:when test="name(.)='not'">
					<xsl:apply-templates select="." mode="not"/>
				</xsl:when>
				<xsl:when test="name(.)='min' or name(.)='max' or name(.)='last' or name(.)='first' or name(.)='second' or name(.)='third' or name(.)='fourth' or name(.)='fifth'">
					<xsl:apply-templates select="." mode="subset"/>
					<xsl:apply-templates select="."/>
				</xsl:when>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	
</xsl:stylesheet>