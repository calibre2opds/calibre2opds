                     README for Calibre2Opds v3.2
                     ------------------------------

Generate OPDS and HTML catalogs from your Calibre ebooks database

The project's home site (http://calibre2opds.org) is the main source for 
documentation.

The Calibre2Opds team :
    * David Pierron - main programmer
    * Dave Walker - guru, features manager and tester extraordinaire
    * Douglas Steele - programmer 
    * Jane Litte - beta tester and moral support

Special thanks to Kb Sriram, who not only programmed Trook, an excellent and OPDS compatible library 
manager for the Nook, but also was kind enough to donate a Nook !

Based on Calibre2Web, a Visual Basic script written by Dave Walker (itimpi) for MobileRead users
  (http://www.mobileread.com/forums/showthread.php?t=33388)
Thanks to him for the idea and the work he spared me !
Also, thanks go to Kovid Goyal for the excellent Calibre
  (http://calibre-ebook.com/)
and to the Lexcycle people for Stanza
   (http://www.lexcycle.com/)

This project uses :
 - JDirectoryChooser from the L2FProd.com library.
 - SQLiteJDBC from http://www.xerial.org/trac/Xerial/wiki/SQLiteJDBC.
 - niceButtons from the Dynamic Drive CSS Library (http://www.dynamicdrive.com/style).
 - breadcrumbs by Janko 
(http://www.jankoatwarpspeed.com/post/2008/08/14/Create-applecom-like-breadcrumb-using-simple-CSS.aspx)

**********************
     1/ Goal
**********************

This tool can read the Calibre database, and extract from it the information needed to generate OPDS 
and HTML catalogs inside the Calibre library folder.
If this folder is published to a web server, or shared using a syncing service (see my thread about 
Calibre + Dropbox on MobileRead : http://www.mobileread.com/forums/showthread.php?p=676809), then 
OPDS-compatible programs (for example Stanza on an iPhone, or Aldiko on Android) can access these 
catalogs, and use them to automatically display your Calibre library in nicely sorted categories.
The last step is to create a cron job that runs every few minutes (I suggest 30) and uses this tool to 
generate the catalogs ; this way, when your computer is up and running, the catalogs are always 
up-to-date, and the syncing service automatically publishes them as they change.
On Mac OS X, it is possible to use Lingon, a free tool (advanced users only) to set up the computer so 
that it automatically starts calibre2opds each time the Calibre database changes...

**************************
     2/ Prerequisites
**************************

 - Java VM version 1.5 minimum
 - Calibre
 - (optional) a way of publishing the catalogs, e.g. Dropbox

********************************
     3/ Using this program
********************************

First, install it to some place on your hard drive. 
 - on Windows, double-click the install.jar file to start the installer program ; alternatively, you 
can start the install.cmd script ; both will need Java 1.5
 - on Mac OS X, drag the application icon to the Applications folder
 - on Linux, , double-click the install.jar file to start the installer program ; 
   you will need Java 1.5

Note that, if you're running an Unix system, you'll need to set execute permissions on the run.sh 
script. This script is located in the install folder, or in the installation disk image on Mac OS X.

The program can be run by double-clicking its application icon in the start menu (Windows) 
or in the Applications folder (Mac OS X). The user interface allows you to configure the program, and 
save this configuration for future runs. You can also directly generate the catalogs.

It is also possible to start the program in command-line mode : run the script (run.cmd on Windows, 
run.sh on Linux and Mac OS X). You can pass parameters to the script, but the default values will be 
the ones you selected in the user interface.

On Mac OS X, the script is located inside the application bundle ; if you installed the program in 
/Applications, for example, then you will find it at 
/Applications/Calibre2Opds.app/Contents/Resources/Java/run.sh

NOTE : for advanced users, the configuration is stored in a hidden file named ".calibre2opds.xml" in 
the user home folder. It is a XML file that you can manually edit, although it is not officially 
supported.

If you need more info, if this FAQ is not clear enough, please ask a question in the Forum: 
  https://getsatisfaction.com/calibre2opds/


******************************************************
     4/ Using Dropbox to share a Calibre library
******************************************************

It all started with a thread on MobileRead, where I explained how I used Dropbox (a free syncing and 
sharing service) to publish my calibre library on the internet, and access it from my iPhone (using 
Stanza) :

http://www.mobileread.com/forums/showthread.php?t=62332

First, install Dropbox (https://www.dropbox.com/install) ; this creates, somewhere in your file system, 
a Dropbox directory (usually called "My Dropbox" or "Dropbox"). As it is explained in the Dropbox FAQ, 
every file you put in this directory, on in a subfolder, will automatically be published to the Dropbox 
"cloud", and synchronized on other computers that are linked to your Dropbox account.

There is a special folder here, which is your public Dropbox folder ; it is called "Public". Everything 
you put in there will be public, which means that if you give the internet address of this folder in 
the Dropbox "cloud" to someone else, he will be able to access your files without your Dropbox 
password. This is important, because for the moment this is the only place where files get consistent 
web addresses, and we will need this later.

Configure Calibre to set up its library in this Public folder (or in a subfolder there) : you can do 
this by opening up preferences and choosing a new folder location. This will take a while.

Wait until Dropbox has uploaded all your books (it may take a while, depending on your upload 
capacity). By the way, Dropbox is free for the first two gigabytes only ; for some of us, who have a 
lot of books, this may not be enough... Time to shell out some cash ;)

Then, download and install the calibre2opds tool, and use it to generate the catalogs as it is 
explained here : http://calibre2opds.com/read-the-documentation/download-and-install/

Again, wait until Dropbox has uploaded all the catalogs and thumbnails.

Configure your ebook reader (Stanza, Aldiko, another OPDS reader, or a simple browser) to access your 
published catalog as explained here : http://calibre2opds.com/read-the-documentation/use-catalogs/

You're done !

Jane Lite has written a nice tutorial on her website : 
http://dearauthor.com/wordpress/2010/02/14/create-your-own-cloud-of-ebooks-with-calibre-calibre-opds-dro
pbox

If you need more info, if this FAQ is not clear enough, please ask a question in the Questions section 
: http://answers.launchpad.net/calibre2opds/+addquestion

******************************************
     5/ Using the generated catalogs
******************************************

The tool generates two sets of catalogs : an OPDS catalog, for use with compatible eBook reading 
programs (for example, Stanza and Aldiko), and an HTML catalog, viewable on any modern browser.

To use them, you have to know where you have published them. If you use Dropbox, it's easy : go to the 
folder where the catalogs where generated (inside the Calibre database folder, the default name of the 
catalog directory is _catalog) and right click the catalog.xml file ; choose "Copy public link" from 
the Dropbox menu, and bingo : the link is stored into your clipboard. If you use any other system, 
you'll have to find out yourself - I may be able to help you, though, if you ask kindly enough ;)

Once you know the address (called an URL) of your catalog.xml file, find a way to send it to your 
iPhone ; I suggest emailing it to yourself and then copy it from the email on the iPhone, saving the 
hustle of typing it all on a phone...

As an example, the address of the demonstration catalog is 
http://davidsoft.free.fr/calibre2opds/demo/_catalog/catalog.xml

Using the OPDS catalog in Stanza :
-----------------------------------------------
Run Stanza on the iPhone, then click on the "Find books" button at the bottom of the screen, then 
choose the "Shared" tab at the top. Click the "Modify" button at the top right, Then choose "Add a book 
source". Give it whatever name suits you, and enter the URL into the obvious field. Make sure to select 
"catalog" as the type of source. Click "Save" at the top right, and you're done...

Using the HTML catalog with Firefox :
--------------------------------------------------
Simply type the URL of the catalog.html file in the address bar, and then browse the catalog. If you 
want to read ePub books directly in your browser, I suggest using the ePubReader add-on.

If you need more info, if this FAQ is not clear enough, please ask a question in the Questions section 
of this site : http://answers.launchpad.net/calibre2opds/+addquestion

**************************************
     6/ Additional information
**************************************

Calibre2opds has its own dedicated home site at http:///www.calibre2opds.com.   Here you will 
find all the documentation and pointers to other resources for supporting calibre2opds.

You can ask questions on the calibre2opds forum at https://getsatisfaction.com/calibre2opds

If you think that you ahve found a bug, or would like to raise a feature request then this can be done 
via the calibre2opds Issue tracking system at http://calibre2opds.myjetbrains.com/youtrack/

Please contact me (dpierron+calibre2opds@gmail.com) if you need more information. 
ugs, but we'll be happy to be informed of them.

If you have too much money, you can always donate some : 
https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=LJJYRJBYCW8EU