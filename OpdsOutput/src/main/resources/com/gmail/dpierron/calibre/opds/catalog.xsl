<xsl:stylesheet exclude-result-prefixes="opds" version="2.0" xmlns:opds="http://www.w3.org/2005/Atom" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!--
  catalog.xsl:    This is the transformation applied to the catalog files to produce the HTML version
                  In OPDS terms these are the files that are of type "feed"
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
  <xsl:param name="intro.userguide"/>
  <xsl:param name="intro.userguide.url"/>
  <xsl:param name="intro.developerguide"/>
  <xsl:param name="intro.developerguide.url"/>
  <xsl:param name="intro.team.title"/>
  <xsl:param name="intro.team.title2"/>
  <xsl:param name="intro.team.list1"/>
  <xsl:param name="intro.team.list2"/>
  <xsl:param name="intro.team.list3"/>
  <xsl:param name="intro.team.list4"/>
  <xsl:param name="intro.thanks.1"/>
  <xsl:param name="intro.thanks.2"/>
  <xsl:template match="/opds:feed">
    <html>
      <head>

        <meta content="yes" name="apple-mobile-web-app-capable"/>
        <meta content="text/html; charset=utf-8" http-equiv="Content-Type"/>
        <meta content="minimum-scale=1.0, width=device-width, maximum-scale=0.6667, user-scalable=no"
          name="viewport"/>
        <meta content="IE=8" http-equiv="X-UA-Compatible"/>
        <xsl:choose >
          <xsl:when test="string-length($programName) > 0">
            <!-- top level catalog entry -->
            <link type="text/css" rel="stylesheet" href="desktop.css"/>
            <link type="text/css" rel="stylesheet"  media="only screen and (max-device-width: 480px)" href="mobile.css"/>
            <script src="functions.js" type="text/javascript"></script>

          </xsl:when>
          <xsl:otherwise>
            <link type="text/css" rel="stylesheet" href="../desktop.css"/>
            <link type="text/css" rel="stylesheet"  media="only screen and (max-device-width: 480px)" href="../mobile.css"/>
            <script src="../functions.js" type="text/javascript"></script>
          </xsl:otherwise>
        </xsl:choose>
        <title>
            <xsl:value-of select="$libraryTitle"/>
        </title>
      </head>
      <body style="">

        <!-- Custom Header fo those who want to add one -->
        <xsl:choose>
          <xsl:when test="string-length($programName) > 0">
            <iframe src="header.html" longdesc="headerInfo" frameBorder="0" scrolling="no" height="35" width="480">Browser not compatible.</iframe>
          </xsl:when>
          <xsl:otherwise>
            <iframe src="../header.html" longdesc="headerInfo" frameBorder="0" scrolling="no" height="35" width="480">Browser not compatible.</iframe>
          </xsl:otherwise>
        </xsl:choose>

        <div id="topbar">
          <div id="title">
            <xsl:value-of select="$libraryTitle"/>
          </div>

          <xsl:choose>
            <xsl:when test="string-length($programName) > 0">
            </xsl:when>
            <xsl:otherwise>
              <div id="leftnav">
                <a href="../index.html">
                    <img alt="home" src="../homeIwebKit.png" />
                </a>
              </div>
            </xsl:otherwise>
          </xsl:choose>
        </div>

        <div class="desktop">
          <h1>
            <xsl:value-of select="$libraryTitle"/>
          </h1>
          <xsl:if test="string-length($programName) > 0">
            <!-- on the main page, let's add the summary -->
            <xsl:copy-of select="opds:content"/>
          </xsl:if>
        </div>

        <!-- Menu bar -->
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
          <!-- Add each entry in body of the list -->
          <xsl:for-each select="opds:entry">
            <xsl:choose>
              <xsl:when test="contains(opds:id, ':book:')">
                <!-- Partial entries for a book -->
                <xsl:variable name="bookId" select="opds:id"/>
                <!-- thumbnail -->
                <div class="x_container" id="{$bookId}">
                  <div class="cover">

                  <xsl:variable name="authorlist">
                  <xsl:for-each select="opds:author">
                    <xsl:if test="string-length(.) > 0">
                      <xsl:choose>
                        <xsl:when test="position() = 1">
                            <xsl:value-of select="opds:name"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="concat(' ',$i18n.and,' ')"/>
                            <xsl:value-of select="opds:name"/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:if>
                  </xsl:for-each>
                  </xsl:variable>
                  <xsl:variable name="bookTitle"><xsl:value-of select="concat($authorlist, ' - ', opds:title)"/></xsl:variable>
                    <a href="{concat(substring-before(opds:link[@type='application/atom+xml;type=entry;profile=opds-catalog'  and @rel='alternate']/@href, '.xml'), '.html')}" title="{$bookTitle}">
                      <xsl:choose>
                        <xsl:when test="opds:link[@rel='http://opds-spec.org/image/thumbnail']">
                          <img src="{opds:link[@rel='http://opds-spec.org/image/thumbnail']/@href}" width="{$thumbWidth}" height="{$thumbHeight}" />
                        </xsl:when>
                        <xsl:otherwise>
                          <img src="../thumbnail.png" width="{$thumbWidth}" height="{$thumbHeight}" />
                        </xsl:otherwise>
                      </xsl:choose>
                     </a>
                  </div>
                  <xsl:if test="$browseByCover != 'true'">
                    <!-- summary -->
                    <div class="details">
                      <!-- title -->
                      <div class="x_title">
                        <a href="{concat(substring-before(opds:link[@type='application/atom+xml;type=entry;profile=opds-catalog'  and @rel='alternate']/@href, '.xml'), '.html')}" title="opds:title">
                        <xsl:value-of select="opds:title"/>
                        </a>
                        <br/>
                        <small>
                          <em>
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
                          </em>
                        </small>

                        <xsl:if test="string-length(opds:summary) > 1">
                          <br/>
                          <small>
                            <xsl:value-of select="opds:summary"/>
                          </small>
                        </xsl:if>
                      </div>
                    </div>
                  </xsl:if>
                </div>
              </xsl:when>
              <xsl:otherwise>
                <!-- Entries that are not for books -->
                <xsl:variable name="url">
                  <xsl:choose>
                    <xsl:when test="substring(opds:id, 1, 29) = 'urn:calibre2opds:externalLink'">
                      <!-- External links with .xml extension need transforming to .html
                           while others (e.g. ,html) ae left alone  -->
                      <xsl:choose>
                        <xsl:when test="opds:link[@type='text/html']/@href">
                          <xsl:value-of select="opds:link/@href"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:value-of select="concat(substring-before(opds:link/@href, '.xml'),'.html')"/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:choose>
                        <xsl:when test="opds:link[@type='application/atom+xml;profile=opds-catalog;kind=navigation']">
                          <xsl:value-of select="concat(substring-before(opds:link[@type='application/atom+xml;profile=opds-catalog;kind=navigation']/@href, '.xml'), '.html')"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:value-of select="opds:link[@type='text/html']/@href"/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:variable>
                  <div class="x_menulisting" id="{opds:id}">
                    <!-- image -->
                    <div class="cover">
                      <xsl:choose>
                        <xsl:when test="opds:link[@rel='http://opds-spec.org/image/thumbnail']">
                          <xsl:element name="a">
                            <xsl:attribute name="href"><xsl:value-of select="$url"/></xsl:attribute>
                            <xsl:attribute name="title"><xsl:value-of select="opds:title"/></xsl:attribute>
                            <xsl:choose>
                              <xsl:when test="opds:link[@target]">
                                <xsl:attribute name="target">_blank</xsl:attribute>
                              </xsl:when>
                            </xsl:choose>
                            <img  src="{opds:link[@rel='http://opds-spec.org/image/thumbnail']/@href}" border="0" />
                          </xsl:element>
                        </xsl:when>
                      </xsl:choose>
                    </div>
                    <!-- Text -->
                    <div class="details">
                      <xsl:element name="a">
                        <xsl:attribute name="href"><xsl:value-of select="$url"/></xsl:attribute>
                        <xsl:attribute name="title"><xsl:value-of select="opds:title"/></xsl:attribute>
                        <xsl:choose>
                          <xsl:when test="opds:link[@target]">
                            <xsl:attribute name="target">_blank</xsl:attribute>
                          </xsl:when>
                        </xsl:choose>
                        <xsl:value-of select="opds:title"/>
                      </xsl:element>
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
        <!-- Add next/previous type buttons -->
        <xsl:if test="opds:link[@rel='next'] or opds:link[@rel='prev']">
          <tr>
            <td width="{$thumbWidth}"/>
            <td width="10px"/>
            <!-- TODO Test version trying to get function call to work
                      Intent is to geneate better navigation than 'next' button -->
            <!--
            <xsl:variable name="url"></xsl:variable>
            <xsl:variable name="url">opds:link[@rel='next']/@href</xsl:variable>
            <xsl:variable name="title">opds:link[@rel='next']/@title</xsl:variable>
            <xsl:value-of select="xsl:multiPage(@url,@title)"/>?
            -->
            <!-- TODO  Original version
            -->
            <td>
              <div class="buttonwrapper">
                <xsl:if test="opds:link[@rel='first']">
                  <a class="ovalbutton" href="{concat(substring-before(opds:link[@rel='first']/@href, '.xml'), '.html')}">
                    <span>
                      <xsl:value-of select="opds:link[@rel='first']/@title"/>
                    </span>
                  </a>
                </xsl:if>
                <xsl:if test="opds:link[@rel='prev']">
                  <a class="ovalbutton" href="{concat(substring-before(opds:link[@rel='prev']/@href, '.xml'), '.html')}">
                    <span>
                      <xsl:value-of select="opds:link[@rel='prev']/@title"/>
                    </span>
                  </a>
                </xsl:if>
                <xsl:if test="opds:link[@rel='next']">
                  <a class="ovalbutton" href="{concat(substring-before(opds:link[@rel='next']/@href, '.xml'), '.html')}">
                    <span>
                      <xsl:value-of select="opds:link[@rel='next']/@title"/>
                    </span>
                  </a>
                </xsl:if>
                <xsl:if test="opds:link[@rel='last']">
                  <a class="ovalbutton" href="{concat(substring-before(opds:link[@rel='last']/@href, '.xml'), '.html')}">
                    <span>
                      <xsl:value-of select="opds:link[@rel='last']/@title"/>
                    </span>
                  </a>
                </xsl:if>
              </div>
            </td>
          </tr>
        </xsl:if>
        <xsl:if test="string-length($programName) > 0">
          <xsl:if test="$generateIndex = 'true'">
            <!-- search link -->
            <div class="x_menulisting" id="calibre:search">
              <div class="cover">
                <img src="search.png" />
              </div>
              <div class="details">
                <a href="_search/search.html" title="Search the books">Search the books</a>
                <br>
                  <small>Search the full-text index of the books (work in progress!)</small>
                </br>
              </div>
            </div>
          </xsl:if>
        </xsl:if>
        
        <hr/>

        <!-- Now for the footer  fields -->

        <!-- Generation summary details -->
        <xsl:choose>
          <xsl:when test="string-length($programName) > 0">
            <iframe src="generated.html" longdesc="headerInfo" frameBorder="0" scrolling="no" height="35" width="480"></iframe>
          </xsl:when>
          <xsl:otherwise>
            <iframe src="../generated.html" longdesc="headerInfo" frameBorder="0" scrolling="no" height="35" width="480"></iframe>
          </xsl:otherwise>
        </xsl:choose>

        <!-- Project details -->
        <xsl:choose>
          <!-- top level catalog entry only -->
          <xsl:when test="string-length($programName) > 0">
            <small>
              <div class="thanks">
                <br/><xsl:value-of select="$intro.goal"/>
                <br/><xsl:value-of select="$intro.wiki.title"/><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text><a href="{$intro.wiki.url}">
                  <xsl:value-of select="$intro.wiki.url"/></a>
                <br/><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text><a href="{$intro.userguide.url}">
                  <xsl:value-of select="$intro.userguide"/> </a>
                <br/><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text><a href="{$intro.developerguide.url}">
                  <xsl:value-of select="$intro.developerguide"/> </a>
                <xsl:if test="string-length($intro.team.title) > 1">
                  <!-- Currently active memeber -->
                  <br/><br/><xsl:value-of select="$intro.team.title"/>
                  <ul>
                  <li><xsl:value-of select="$intro.team.list1"/></li>
                  </ul>
                  <xsl:if test="string-length($intro.team.title2) > 1">
                    <!-- Previously active members -->
                    <xsl:value-of select="$intro.team.title2"/>
                    <ul>
                    <xsl:if test="string-length($intro.team.list2) > 1">
                      <li><xsl:value-of select="$intro.team.list2"/></li>
                    </xsl:if>
                    <xsl:if test="string-length($intro.team.list3) > 1">
                      <li><xsl:value-of select="$intro.team.list3"/></li>
                    </xsl:if>
                    <xsl:if test="string-length($intro.team.list4) > 1">
                      <li><xsl:value-of select="$intro.team.list4"/></li>
                    </xsl:if>
                    </ul>
                  </xsl:if>
                </xsl:if>
                <xsl:if test="string-length($intro.thanks.1) > 1">
                  <xsl:value-of select="$intro.thanks.1"/>
                  <br/><xsl:value-of select="$intro.thanks.2"/>
                </xsl:if>
              </div>
            </small>
          </xsl:when>
        </xsl:choose>


        <div id="footer">
          <!-- Support iWebKit by sending them traffic -->
          <a class="noeffect" href="http://snippetspace.com">Powered by iWebKit</a>
        </div>

      </body>
    </html>
  </xsl:template>

  <!-- Function to try and implement improved navigation for multi-page sets -->
  <!-- TODO NOT YET READY FOR USE -->
  <xsl:function name="xsl:multiPage">
    <xsl:param name="url"/>
    <xsl:param name="title"/>
    <td>
      <div class="buttonwrapper">
        <!-- <a class="ovalbutton" href="{concat(substring-before(opds:link[@rel='next']/@href, '.xml'), '.html')}"> -->
        <a class="ovalbutton" href="{concat(substring-before($url, '.xml'), '.html')}">
          <span>
            <!-- <xsl:value-of "$titleopds:link[@rel='next']/@title"/> -->
            <xsl:value-of select="@title"/>
          </span>
        </a>
      </div>
    </td>
  </xsl:function>
</xsl:stylesheet>
