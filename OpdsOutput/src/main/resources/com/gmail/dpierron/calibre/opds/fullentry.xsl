<xsl:stylesheet exclude-result-prefixes="opds" version="!.0" xmlns:opds="http://www.w3.org/2005/Atom" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:rsl="http://www.w3.org/1999/XSL/Transform">
<!--
  fullentry.xsl:    This is the transformation applied to the book details files to produce the HTML version
                    In OPDS terms these are the files that are of type "entry"
-->
  <xsl:output method="html" version="4.01"/>
  <xsl:output doctype-public="-//W3c//DTD html 4.01//EN"/>
  <xsl:output doctype-system="http://www.w3c.org/tr/html4/strict.dtd"/>
  <xsl:output indent="yes"/>
  <xsl:param name="libraryTitle"/>
  <xsl:param name="programName"/>
  <xsl:param name="programVersion"/>
  <xsl:param name="coverWidth">336</xsl:param>
  <xsl:param name="coverHeight">500</xsl:param>
  <xsl:param name="thumbWidth" />
  <xsl:param name="thumbHeight" />
  <xsl:param name="generateDownloads">true</xsl:param>
  <xsl:param name="generateIndex">false</xsl:param>
  <xsl:param name="browseByCover">false</xsl:param>
  <xsl:param name="i18n.and"/>
  <xsl:param name="i18n.dateGenerated"/>
  <xsl:param name="i18n.backToMain"/>
  <xsl:param name="i18n.summarysection"/>
  <xsl:param name="i18n.downloads"/>
  <xsl:param name="i18n.links"/>
  <xsl:param name="i18n.downloadfile"/>
  <xsl:param name="i18n.downloadsection"/>
  <xsl:param name="i18n.coversection"/>
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
  <xsl:template match="/opds:entry">
    <html>
      <head>

        <meta content="yes" name="apple-mobile-web-app-capable"/>
        <meta content="text/html; charset=utf-8" http-equiv="Content-Type"/>
        <meta content="minimum-scale=1.0, width=device-width, maximum-scale=0.6667, user-scalable=no" name="viewport"/>
        <meta content="IE=8" http-equiv="X-UA-Compatible"/>
        <link type="text/css" rel="stylesheet" href="../desktop.css"/>
        <link type="text/css" rel="stylesheet"  media="only screen and (max-device-width: 480px)" href="../mobile.css"/>
        <script src="../functions.js" type="text/javascript"></script>
        <xsl:text disable-output-escaping="yes">&lt;title&gt;</xsl:text>
          <xsl:value-of select="$libraryTitle"/>
        <xsl:text disable-output-escaping="yes">&lt;/title&gt;</xsl:text>
      </head>
      <body style="">
        <!-- Custom Header for those who want to add one -->
        <iframe src="../header.html" longdesc="headerInfo" frameBorder="0" scrolling="no" height="35" width="480">Browser not compatible.</iframe>

        <div id="topbar">
          <div id="title">
            <xsl:value-of select="$libraryTitle"/>
          </div>

          <div id="leftnav">
            <a href="../index.html"><img alt="home" src="../homeIwebKit.png" /></a>
          </div>
        </div>

        <div class="desktop">
          <h1><xsl:value-of select="$libraryTitle"/></h1>
        </div>

        <div class="desktop">
          <ul id="breadcrumb">
            <xsl:for-each select="opds:link[@rel='breadcrumb']">
              <xsl:text disable-output-escaping="yes">&lt;li&gt;</xsl:text>
                <a href="{concat(substring-before(@href, '.xml'), '.html')}" title="{@title}">
                  <xsl:value-of select="@title"/>
                </a>
              <xsl:text disable-output-escaping="yes">&lt;/li&gt;</xsl:text>
            </xsl:for-each>
            <xsl:text disable-output-escaping="yes">&lt;li&gt;</xsl:text>
              <xsl:value-of select="opds:title"/>
            <xsl:text disable-output-escaping="yes">&lt;/li&gt;</xsl:text>
          </ul>
        </div>

        <xsl:variable name="mainDivClassName">
          <xsl:choose>
            <xsl:when test="$browseByCover = 'true'">browseByCover</xsl:when>
            <xsl:otherwise>browseByList</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <div class="{$mainDivClassName}">
          <xsl:variable name="bookId" select="opds:id"/>
          <!-- full entry -->
          <div class="x_container" id="{$bookId}">

            <!-- title -->
            <div class="fullEntry_title">
              <h1>
                <xsl:value-of select="opds:title"/>
                <br/>
                <small><em><small><em>
                  <xsl:for-each select="opds:author">
                    <xsl:variable name="uri">
                      <xsl:value-of select="opds:uri"/>
                    </xsl:variable>
                    <xsl:if test="string-length(.) > 0">
                      <xsl:choose>
                        <xsl:when test="position() = 1">
                          <xsl:element name="a">
                            <xsl:attribute name="href">
                              <xsl:value-of select="concat(substring-before($uri, '.xml'),'.html')"/>
                            </xsl:attribute>
                            <xsl:value-of select="opds:name"/>
                          </xsl:element>
                        </xsl:when>
                        <xsl:otherwise>
                          <small>
                            <xsl:value-of select="concat(' ',$i18n.and,' ')"/>
                          </small>
                          <xsl:element name="a">
                            <xsl:attribute name="href">
                              <xsl:value-of select="concat(substring-before($uri, '.xml'),'.html')"/>
                            </xsl:attribute>
                            <xsl:value-of select="opds:name"/>
                          </xsl:element>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:if>
                  </xsl:for-each>
                </em></small></em></small>
              </h1>
            </div>

            <!-- downloads -->
            <div class="fullEntry_downloads">
              <xsl:if test="$generateDownloads = 'true'">
                <xsl:text disable-output-escaping="yes">&lt;h2 class="fullEntry_sectionHeader"&gt;</xsl:text>
                  <xsl:value-of select="$i18n.downloadsection"/>
                <xsl:text disable-output-escaping="yes">&lt;/h2&gt;</xsl:text>
                <ul>
                  <xsl:if test="opds:link[@rel='http://opds-spec.org/acquisition']">
                    <xsl:for-each select="opds:link[@rel='http://opds-spec.org/acquisition']">
                      <xsl:variable name="displaysize">
                          <xsl:value-of select="./@displaysize"/>
                       </xsl:variable>
                      <rsl:variable name="hrefval">
                        <xsl:value-of select="./@href"/>
                      </rsl:variable>
                      <xsl:text disable-output-escaping="yes">&lt;li&gt;</xsl:text>
                        <xsl:element name="a">
                          <xsl:attribute name="href">
                            <xsl:value-of select="$hrefval"/>
                          </xsl:attribute>
                          <xsl:attribute name="type">
                            <xsl:value-of select="./@type"/>
                          </xsl:attribute>
                          <!-- #c20-277  Add 'download' attribute to acquisition links -->
                          <!-- #c2o-280  Add 'download' attribute by taking name from URL -->
                          <xsl:attribute name="download">
                            <xsl:call-template name="substring-after-last">
                              <xsl:with-param name="string" select="$hrefval"/>
                              <xsl:with-param name="char" select="'/'"/>
                            </xsl:call-template>
                          </xsl:attribute>
                          <xsl:choose>
                            <xsl:when test="string-length(@title) > 0">
                              <xsl:value-of select="./@title"/>
                            </xsl:when>
                            <xsl:otherwise>
                              <xsl:value-of select="$i18n.downloadfile"/>
                            </xsl:otherwise>
                          </xsl:choose>
                        </xsl:element>
                        <xsl:if test="string-length($displaysize) > 0">
                          <xsl:text> (</xsl:text><xsl:value-of select="$displaysize"/><xsl:text>)</xsl:text>
                        </xsl:if>
                      <xsl:text disable-output-escaping="yes">&lt;/li&gt;</xsl:text>
                    </xsl:for-each>
                  </xsl:if>
                </ul>
              </xsl:if>
            </div>

            <!-- cover -->
            <div class="fullEntry_cover">
              <h2 class="fullEntry_sectionHeader"><xsl:value-of select="$i18n.coversection"/></h2>
              <xsl:choose>
                <xsl:when test="opds:link[@rel='http://opds-spec.org/image']">
                  <img width="{$coverWidth}" height="{$coverHeight}" alt="{opds:title}" src="{opds:link[@rel='http://opds-spec.org/image']/@href}" />
                </xsl:when>
                <xsl:otherwise>

                </xsl:otherwise>
              </xsl:choose>
            </div>

            <!-- summary -->
            <div class="fullEntry_summary">
              <xsl:if test="string-length(opds:content) > 1">
                <xsl:text disable-output-escaping="yes">&lt;h2 class="fullEntry_sectionHeader"&gt;</xsl:text>
                  <xsl:value-of select="$i18n.summarysection"/>
                <xsl:text disable-output-escaping="yes">&lt;/h2&gt;</xsl:text>
                <xsl:text disable-output-escaping="yes">&lt;p&gt;</xsl:text>
                  <xsl:if test="string-length(opds:content) > 0">
                    <xsl:copy-of select="opds:content"/>
                  </xsl:if>
                <xsl:text disable-output-escaping="yes">&lt;/p&gt;</xsl:text>
              </xsl:if>
            </div>

            <!-- related catalogs -->
            <div class="fullEntry_related">
              <xsl:if test="opds:link[@rel='related' and @type='application/atom+xml;profile=opds-catalog;kind=navigation']">
                <h2 class="fullEntry_sectionHeader"><xsl:value-of select="$i18n.relatedsection"/></h2>
                <ul>
                  <xsl:for-each select="opds:link[@rel='related' and @type='application/atom+xml;profile=opds-catalog;kind=navigation']">
                    <xsl:text disable-output-escaping="yes">&lt;li&gt;</xsl:text>
                      <xsl:element name="a">
                        <xsl:attribute name="href">
                          <xsl:value-of select="concat(substring-before(@href, '.xml'), '.html')"/>
                        </xsl:attribute>
                        <xsl:value-of select="@title"/>
                      </xsl:element>
                    <xsl:text disable-output-escaping="yes">&lt;/li&gt;</xsl:text>
                  </xsl:for-each>
                </ul>
              </xsl:if>
            </div>

            <!-- external links -->
            <div class="fullEntry_links">
              <xsl:if test="opds:link[@rel='related' and @type='text/html']">
                <h2 class="fullEntry_sectionHeader"><xsl:value-of select="$i18n.linksection"/></h2>
                <ul>
                  <xsl:for-each select="opds:link[@rel='related' and @type='text/html']">
                    <xsl:text disable-output-escaping="yes">&lt;li&gt;</xsl:text>
                      <xsl:element name="a">
                        <xsl:attribute name="href"><xsl:value-of select="@href"/></xsl:attribute>
                        <xsl:choose>
                          <xsl:when test="@target">
                           <xsl:attribute name="target"><xsl:value-of select="@target"/></xsl:attribute>
                          </xsl:when>
                        </xsl:choose>
                        <xsl:value-of select="@title"/>
                      </xsl:element>
                    <xsl:text disable-output-escaping="yes">&lt;/li&gt;</xsl:text>
                  </xsl:for-each>
                </ul>
              </xsl:if>
            </div>
          </div>
      </div>

        <iframe src="../generated.html" longdesc="headerInfo" frameBorder="0" scrolling="no" height="35" width="480">Generation summary details</iframe>
      <xsl:text disable-output-escaping="yes">&lt;div id="footer"&gt;</xsl:text>
        <!-- Support iWebKit by sending them traffic -->
        <a class="noeffect" href="http://snippetspace.com">Powered by iWebKit</a>
      <xsl:text disable-output-escaping="yes">&lt;/div&gt;</xsl:text>

      </body>
    </html>
  </xsl:template>   <!-- entry -->

  <xsl:template name="substring-after-last">
    <xsl:param name="string"/>
    <xsl:param name="char"/>

    <xsl:choose>
      <xsl:when test="contains($string, $char)">
        <xsl:call-template name="substring-after-last">
          <xsl:with-param name="string" select="substring-after($string, $char)"/>
          <xsl:with-param name="char" select="$char"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$string"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
