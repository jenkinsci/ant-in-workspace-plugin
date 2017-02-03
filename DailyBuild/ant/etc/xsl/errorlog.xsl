<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:preserve-space elements="*"/>
<xsl:output method="xml" indent="yes" encoding="UTF-8"/>
<xsl:param name="errorfile"/>
  
<xsl:template match="/">
<errorlog xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../../../DailyBuild/xsd/errorlog.xsd">
  <xsl:variable name="errordoc" select="document($errorfile)"/>
  <errorNumbers>
    <xsl:apply-templates select="errorlog/errorNumbers/*"/>
    <xsl:apply-templates select="$errordoc/errorlog/errorNumbers/*"/>
  </errorNumbers>
  <errorCodes>
    <xsl:apply-templates select="errorlog/errorCodes/*"/>
    <xsl:apply-templates select="$errordoc/errorlog/errorCodes/*"/>
  </errorCodes>
</errorlog>
</xsl:template>

<xsl:template match="errorNumber|errorCode">
  <xsl:copy-of select="."/>
</xsl:template>
      
</xsl:stylesheet>

