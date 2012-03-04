<xsl:stylesheet exclude-result-prefixes="opds" version="1.0" xmlns:opds="http://www.w3.org/2005/Atom" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
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
                <meta content="minimum-scale=1.0, width=device-width, maximum-scale=0.6667, user-scalable=no"
                      name="viewport"/>
                <meta content="IE=8" http-equiv="X-UA-Compatible"/>
                <link type="text/css" rel="stylesheet" href="../desktop.css"/>
                <link type="text/css" rel="stylesheet"  media="only screen and (max-device-width: 480px)" href="../mobile.css"/>
                <script src="../functions.js" type="text/javascript"></script>
                <title>
                    <xsl:value-of select="$libraryTitle"/>
                </title>
                <script type="text/javascript">function showHide(id, btn) { var e = document.getElementById(id); if (e.style.display == 'none') { e.style.display = 'block'; } else { e.style.display = 'none'; } } </script>
                <script type="text/javascript">function getBaseURL() {var url = location.href;var baseURL = url.substring(0, url.lastIndexOf('/'));if (baseURL.indexOf('http://localhost') != -1) {var url = location.href;var pathname = location.pathname;var index1 = url.indexOf(pathname);var index2 = url.indexOf("/", index1 + 1);var baseLocalUrl = url.substr(0, index2);return baseLocalUrl + "/";} else {return baseURL + "/";}}</script>
            </head>
            <body style="">

                <div id="topbar">
                    <div id="title">
                        <xsl:value-of select="$libraryTitle"/>
                    </div>


                    <div id="leftnav">
                        <a href="../index.html">
                            <img alt="home" src="../homeIwebKit.png"/>
                        </a>
                    </div>
                </div>

                <div class="desktop">
                    <h1>
                        <xsl:value-of select="$libraryTitle"/>
                    </h1>
                </div>

                <iframe src="../header.html" longdesc="headerInfo" frameBorder="0" scrolling="no" height="35">
                    Browser not compatible.
                </iframe>

                <div class="desktop">
                <ul id="breadcrumb">
                        <xsl:for-each select="opds:link[@rel='breadcrumb']">
                            <li>
                                <a href="{concat(substring-before(@href, '.xml'), '.html')}" title="{@title}">
                                    <xsl:value-of select="@title"/>
                                </a>
                            </li>
                        </xsl:for-each>
                        <li>
                            <xsl:value-of select="opds:title"/>
                        </li>
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
                                        <xsl:for-each select="opds:author/opds:name">
                                            <xsl:if test="string-length(.) > 0">
                                                <xsl:choose>
                                                    <xsl:when test="position() = 1">
                                                        <xsl:value-of select="."/>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <small>
                                                          <xsl:value-of select="concat(' ',$i18n.and,' ')"/>
                                                        </small>
                                                        <xsl:value-of select="."/>
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
                                    <h2 class="fullEntry_sectionHeader"><xsl:value-of select="$i18n.downloadsection"/></h2>
                                    <ul>
                                        <xsl:if test="opds:link[@rel='http://opds-spec.org/acquisition']">
                                            <xsl:if test="opds:link[@rel='http://opds-spec.org/acquisition' and @type='application/epub+zip']">
                                                <li>
                                                    <a href="{opds:link[@rel='http://opds-spec.org/acquisition' and @type='application/epub+zip']/@href}">
                                                        <xsl:value-of select="opds:link[@rel='http://opds-spec.org/acquisition' and @type='application/epub+zip']/@title"/>
                                                    </a>
                                                </li>
                                            </xsl:if>
                                            <xsl:for-each select="opds:link[@rel='http://opds-spec.org/acquisition']">
                                                <xsl:if test="not(@type='application/epub+zip')">
                                                    <li>
                                                        <xsl:element name="a">
                                                            <xsl:attribute name="href">
                                                                <xsl:value-of select="./@href"/>
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
                                                    </li>
                                                </xsl:if>
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
                                        <img    width="{$coverWidth}" height="{$coverHeight}" alt="{opds:title}" src="{opds:link[@rel='http://opds-spec.org/image']/@href}" />
                                    </xsl:when>
                                    <xsl:otherwise>

                                    </xsl:otherwise>
                                </xsl:choose>
                            </div>

                            <!-- summary -->
                            <div class="fullEntry_summary">
                                <xsl:if test="string-length(opds:content) > 1">
                                    <h2 class="fullEntry_sectionHeader"><xsl:value-of select="$i18n.summarysection"/></h2>
                                    <p>
                                        <xsl:if test="string-length(opds:content) > 0">
                                            <xsl:copy-of select="opds:content"/>
                                        </xsl:if>
                                    </p>
                                </xsl:if>
                            </div>

                            <!-- related catalogs -->
                            <div class="fullEntry_related">
                                <xsl:if test="opds:link[@rel='related' and @type='application/atom+xml;profile=opds-catalog;kind=navigation']">
                                    <h2 class="fullEntry_sectionHeader"><xsl:value-of select="$i18n.relatedsection"/></h2>
                                    <ul>
                                        <xsl:for-each select="opds:link[@rel='related' and @type='application/atom+xml;profile=opds-catalog;kind=navigation']">
                                            <li>
                                                <xsl:element name="a">
                                                    <xsl:attribute name="href">
                                                        <xsl:value-of select="concat(substring-before(@href, '.xml'), '.html')"/>
                                                    </xsl:attribute>
                                                    <xsl:value-of select="@title"/>
                                                </xsl:element>
                                            </li>
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
                                            <li>
                                                <xsl:element name="a">
                                                    <xsl:attribute name="href">
                                                        <xsl:value-of select="@href"/>
                                                    </xsl:attribute>
                                                    <xsl:value-of select="@title"/>
                                                </xsl:element>
                                            </li>
                                        </xsl:for-each>
                                    </ul>
                                </xsl:if>
                            </div>
                        </div>
                </div>

                <div id="footer">
                    <!-- Support iWebKit by sending them traffic -->
                    <a class="noeffect" href="http://snippetspace.com">Powered by iWebKit</a>
                </div>

            </body>
        </html>
    </xsl:template>   <!-- entry -->
</xsl:stylesheet>
