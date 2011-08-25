<xsl:stylesheet exclude-result-prefixes="opds" version="1.0" xmlns:opds="http://www.w3.org/2005/Atom" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
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
    <xsl:template match="/opds:feed">
        <html>
            <head>
                <meta content="IE=8" http-equiv="X-UA-Compatible"/>
                <xsl:choose >
                    <xsl:when test="string-length($programName) > 0">
                        <!-- main catalog -->
                        <link href="catalog.css" rel="stylesheet" type="text/css"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <link href="../catalog.css" rel="stylesheet" type="text/css"/>
                    </xsl:otherwise>
                </xsl:choose>
                <title>
                    <xsl:value-of select="$libraryTitle"/>
                </title>
                <script type="text/javascript">function showHide(id, btn) { var e = document.getElementById(id); if (e.style.display == 'none') { e.style.display = 'block'; } else { e.style.display = 'none'; } } </script>
                <script type="text/javascript">function getBaseURL() {var url = location.href;var baseURL = url.substring(0, url.lastIndexOf('/'));if (baseURL.indexOf('http://localhost') != -1) {var url = location.href;var pathname = location.pathname;var index1 = url.indexOf(pathname);var index2 = url.indexOf("/", index1 + 1);var baseLocalUrl = url.substr(0, index2);return baseLocalUrl + "/";} else {return baseURL + "/";}}</script>
            </head>
            <body style="">
                <h1>
                    <xsl:value-of select="$libraryTitle"/>
                </h1>
                <xsl:if test="string-length($programName) > 0">
                    <!-- on the main page, let's add the summary -->
                    <xsl:copy-of select="opds:content"/>
                </xsl:if>
                <p class="dateGenerated">
                    <small>
                        <xsl:value-of select="$i18n.dateGenerated"/>
                    </small>
                </p>
                <p/>
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
                <xsl:variable name="mainDivClassName">
                    <xsl:choose>
                        <xsl:when test="$browseByCover = 'true'">browseByCover</xsl:when>
                        <xsl:otherwise>browseByList</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <div class="{$mainDivClassName}">
                    <xsl:for-each select="opds:entry">
                        <xsl:choose>
                            <xsl:when test="contains(opds:id, ':book:')">
                                <xsl:variable name="bookId" select="opds:id"/>
                                <xsl:choose>
                                    <xsl:when test="not(contains(/opds:feed/opds:id, 'calibre:book:'))">
                                        <!-- partial entry -->
                                        <div class="x_container" id="{$bookId}">
                                            
                                            <xsl:variable name="bookTitle"><xsl:value-of select="concat(opds:author/opds:name, ' - ', opds:title)"/></xsl:variable>
                                            
                                            <!-- thumbnail -->
                                            <div class="cover">
                                                <a href="{concat(substring-before(opds:link[@type='application/atom+xml'  and @rel='alternate']/@href, '.xml'), '.html')}" title="{$bookTitle}">
                                                    <xsl:choose>
                                                        <xsl:when test="opds:link[@rel='http://opds-spec.org/thumbnail']">
                                                            <img  width="{$thumbWidth}" height="{$thumbHeight}" alt="{opds:title}" src="{opds:link[@rel='http://opds-spec.org/thumbnail']/@href}" title="{$bookTitle}" />    
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <img  width="{$thumbWidth}" height="{$thumbHeight}" alt="{opds:title}" src="../default_thumbnail.png" title="{$bookTitle}" />    
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </a>
                                            </div>
                                            
                                            <xsl:if test="$browseByCover != 'true'">
                                                <div class="details">
                                                    <!-- title -->
                                                    <div class="x_title">
                                                        <xsl:value-of select="opds:title"/>        
                                                        <xsl:if test="string-length(opds:author/opds:name) > 1">
                                                            <br/>
                                                            <small>
                                                                <em>
                                                                    <xsl:value-of select="opds:author/opds:name"/>
                                                                </em>
                                                            </small>
                                                        </xsl:if>
                                                        <xsl:if test="string-length(opds:content) > 1">
                                                            <br/>
                                                            <small>
                                                                <xsl:value-of select="opds:content"/>
                                                            </small>
                                                        </xsl:if>                                                    
                                                    </div>
                                                </div>
                                            </xsl:if>
                                        </div>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <!-- full entry -->
                                        <div class="x_container" id="{$bookId}">
                                            
                                            <!-- title -->
                                            <div class="fullEntry_title">
                                                <h1>
                                                    <xsl:value-of select="opds:title"/>        
                                                    <br/>
                                                    <small>
                                                        <em>
                                                            <xsl:if test="string-length(opds:author/opds:name) > 0">
                                                                <small>
                                                                    <em>
                                                                        <xsl:value-of select="opds:author/opds:name"/>
                                                                    </em>
                                                                </small>
                                                            </xsl:if>
                                                        </em>
                                                    </small>
                                                </h1>
                                            </div>
                                            
                                            <!-- cover -->
                                            <div class="fullEntry_cover">
                                                <xsl:choose>
                                                    <xsl:when test="opds:link[@rel='http://opds-spec.org/cover']">
                                                        <img  alt="{opds:title}" src="{opds:link[@rel='http://opds-spec.org/cover']/@href}" />    
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
                                            
                                            <!-- related catalogs -->
                                            <div class="fullEntry_related">
                                                <xsl:if test="opds:link[@rel='related' and @type='application/atom+xml']">
                                                    <h2 class="fullEntry_sectionHeader"><xsl:value-of select="$i18n.relatedsection"/></h2>
                                                    <ul>
                                                        <xsl:for-each select="opds:link[@rel='related' and @type='application/atom+xml']">
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
                                    </xsl:otherwise>
                                </xsl:choose>
                                
                            </xsl:when>
                            <xsl:otherwise>
                                <div class="x_menulisting" id="opds:id">
                                    <div class="cover">
                                        <img  src="{opds:link[@rel='http://opds-spec.org/thumbnail']/@href}" />
                                    </div>
                                    <xsl:variable name="url">
                                        <xsl:choose>
                                            <xsl:when test="opds:link[@type='application/atom+xml']">
                                                <xsl:value-of select="concat(substring-before(opds:link[@type='application/atom+xml']/@href, '.xml'), '.html')"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="opds:link[@type='text/html']/@href"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:variable>
                                    <div class="details">
                                        <a href="{$url}" title="{opds:content}">
                                            <xsl:value-of select="opds:title"/>
                                        </a>
                                        <br/>
                                        <small>
                                            <xsl:value-of select="opds:content"/>
                                        </small>
                                    </div>
                                </div>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </div>
                <xsl:if test="opds:link[@rel='next']">
                    <tr>
                        <td width="{$thumbWidth}"/>
                        <td width="10px"/>
                        <td>
                            <div class="buttonwrapper">
                                <a class="ovalbutton" href="{concat(substring-before(opds:link[@rel='next']/@href, '.xml'), '.html')}">
                                    <span>
                                        <xsl:value-of select="opds:link[@rel='next']/@title"/>
                                    </span>
                                </a>
                            </div>
                        </td>
                    </tr>
                </xsl:if>
                <xsl:if test="string-length($programName) > 0">
                    <p/>
                    <p/>
                    <p/>
                    <p/>
                    <hr/>
                    <!-- on the main page, let's talk -->
                    <small>
                        <br/><xsl:value-of select="$programName"/> v <xsl:value-of select="$programVersion"/>
                        <br/>
                        <br/><xsl:value-of select="$intro.goal"/>
                        <br/><xsl:value-of select="$intro.wiki.title"/><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text><a href="{$intro.wiki.url}">
                            <xsl:value-of select="$intro.wiki.url"/>
                        </a>
                        <br/><xsl:value-of select="$intro.team.title"/>
                        <ul>
                            <li>
                                <xsl:value-of select="$intro.team.list1"/>
                            </li>
                            <li>
                                <xsl:value-of select="$intro.team.list2"/>
                            </li>
                            <li>
                                <xsl:value-of select="$intro.team.list3"/>
                            </li>
                            <li>
                                <xsl:value-of select="$intro.team.list4"/>
                            </li>
                        </ul>
                        <br/><xsl:value-of select="$intro.thanks.1"/>
                        <br/><xsl:value-of select="$intro.thanks.2"/>
                    </small>
                </xsl:if>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
