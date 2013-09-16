<xsl:stylesheet exclude-result-prefixes="opds" version="1.0" xmlns:opds="http://www.w3.org/2005/Atom"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" version="4.01"/>
  <xsl:output doctype-public="-//W3c//DTD html 4.01//EN"/>
  <xsl:output doctype-system="http://www.w3c.org/tr/html4/strict.dtd"/>
  <xsl:output indent="yes"/>
  <xsl:param name="libraryTitle"/>
  <xsl:param name="programName"/>
  <xsl:param name="programVersion"/>
  <xsl:param name="thumbWidth">84</xsl:param>
  <xsl:param name="thumbHeight">125</xsl:param>
  <xsl:param name="generateDownloads">true</xsl:param>
  <xsl:param name="browseByCover">false</xsl:param>
  <xsl:param name="i18n.and"/>
  <xsl:param name="i18n.dateGenerated"/>
  <xsl:param name="i18n.backToMain"/>
  <xsl:param name="i18n.summarysection"/>
  <xsl:param name="i18n.downloads"/>
  <xsl:param name="i18n.links"/>
  <xsl:param name="i18n.downloadfile"/>
  <xsl:param name="i18n.downloadsection"/>

  <xsl:param name="i18n.relatedsection"/>
  <xsl:param name="i18n.linksection"/>
  <xsl:param name="intro.goal"/>
  <xsl:param name="intro.wiki.title"/>
  <xsl:param name="intro.wiki.url"/>
  <xsl:param name="intro.team.title"/>
  <xsl:param name="intro.team.list1"/>
  <xsl:param name="intro.team.list2"/>
  <xsl:param name="intro.team.list3"/>
  <xsl:param name="intro.team.list4"/>
  <xsl:param name="intro.thanks.1"/>
  <xsl:param name="intro.thanks.2"/>


  <xsl:template match="/">
    <p class="dateGenerated">
      <small>
        <xsl:value-of select="$i18n.dateGenerated"/>
      </small>
    </p>
  </xsl:template>

</xsl:stylesheet>