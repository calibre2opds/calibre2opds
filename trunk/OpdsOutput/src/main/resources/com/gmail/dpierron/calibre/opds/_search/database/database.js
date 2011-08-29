var db = openDatabase('c2opds','1.0','Calibre2Opds DB, enabling search feature', 2 * 1024 * 1024);
var default_number_of_keywords = 100;

function createAndPopulateDB(){
cleanDb();
createDb();
populateDb();
}


function createDb(){
db.transaction( function(tx){
tx.executeSql('CREATE TABLE IF NOT EXISTS BOOKS(BK_ID PRIMARY KEY, BK_TITLE, BK_URL, BK_COVER_URL)');
tx.executeSql('CREATE TABLE IF NOT EXISTS KEYWORDS (KW_ID PRIMARY KEY, KW_WORD, KW_WEIGHT INT)');
tx.executeSql('CREATE TABLE IF NOT EXISTS CATALOG_ITEMS (KW_ID, BK_ID, CAT_TYPE)');
});

}


function cleanDb(){
db.transaction(function(tx){
tx.executeSql('DROP TABLE BOOKS');
tx.executeSql('DROP TABLE KEYWORDS');
tx.executeSql('DROP TABLE CATALOG_ITEMS');
});

}

function searchKeyWord(keyWord, element){
cleanSearch();
element.innerHTML ="Nothing found...";
db.readTransaction(function(tx){
tx.executeSql("SELECT * FROM KEYWORDS WHERE KW_WORD like ?",['%'+keyWord+'%'], function (tx,results){							
		if (results.rows.length > 0){
		if (results.rows.length == 1) 
		{
			// If only 1 keyword found, don't bother asking the user if he wants to display books for this tag, do it directly ... 
			return searchByKeywordId(results.rows.item(0).KW_ID,element);
		
		}
		var html = "<table>";		
		for(var i = 0; i < results.rows.length; i++){
		var row = [];
		html += "<tr>";
		html += "<td>";
		html += results.rows.item(i).KW_WORD;
		html += "</td>";
		html += "<td>";		
		html +=	"<a href='#' onclick=\"searchByKeywordId('"+results.rows.item(i).KW_ID+"',document.getElementById('"+ element.id+"'))\">Get</a> &nbsp" ;					
		html += "</td>";
		html +="</tr>";		
		}						
		html += "</table>";
		element.innerHTML = html;
		}
	});
});

}

function populateDb(){
db.transaction(function(tx){

console.log("populate BOOKS table");
var books = getBooks();
for (var i = 0; i< books.length; i++){
tx.executeSql('INSERT INTO BOOKS (BK_ID, BK_TITLE, BK_URL, BK_COVER_URL) VALUES (?, ?, ?, ?)', [books[i][0],books[i][1],books[i][2],books[i][3]]);
}

console.log("finish loading books");

console.log("populate catalogitems table");
var catalogitems = getCatalogitems();
for (var i = 0; i < catalogitems.length; i ++){
tx.executeSql('INSERT INTO CATALOG_ITEMS (KW_ID, BK_ID, CAT_TYPE) VALUES (?, ?, ?)', [catalogitems[i][0],catalogitems[i][1],catalogitems[i][2]]);
}
console.log("finish loading catalogitems table");


console.log("populate keywords");
var keywords = getKeywords();
for (var i = 0; i < keywords.length; i++){
tx.executeSql('INSERT INTO KEYWORDS (KW_ID, KW_WORD,KW_WEIGHT) VALUES (?, ?, ?)', [keywords[i][0],keywords[i][1],keywords[i][2]]);
}
console.log("finish loading keywords");

});

}

function searchByKeywordId(kw_id, element){
cleanSearch();
element.innerHTML ="No result...";
db.transaction(function(tx){
tx.executeSql("SELECT b.BK_TITLE,b.BK_URL, b.BK_COVER_URL FROM KEYWORDS k INNER JOIN CATALOG_ITEMS c on k.KW_ID = c.KW_ID INNER JOIN BOOKS b on c.BK_ID = b.BK_ID  WHERE k.KW_ID = ? GROUP BY b.BK_TITLE,b.BK_URL"
,[kw_id], function (tx,results){					
		if (results.rows.length >0){
		var html = "<table>";		
		for(var i = 0; i < results.rows.length; i++){
		var row = [];
		
		html += "<tr>";		
		html += "<td>";
		html += "<a href='"+results.rows.item(i).BK_URL+"'>";
		html += "<img src=\""+ results.rows.item(i).BK_COVER_URL +"\"/>";
		html += "</a>";
		html +="</td>";
		html += "<td>";
		html += "<a href='"+results.rows.item(i).BK_URL+"'>";
		html += results.rows.item(i).BK_TITLE;
		html += "</a>";
		html += "</td>";				
		html +="</tr>";
		}						
		html += "</table>";
		element.innerHTML = html;
		}
	});
});
}

var level_10;
var level_9;
var level_8;
var level_7;
var level_6;
var level_5;
var level_4;
var level_3;
var level_2;
var level_1;

var diff;
var dist;

function getKeywordsWithSize(element,word){
cleanSearch();
element.innerHTML="";
db.transaction(function(tx){
tx.executeSql("SELECT * FROM KEYWORDS WHERE KW_WORD like ? order by KW_WEIGHT desc",['%'+word+'%'],function (tx,results){					
		var tagClouds="";
		if (results.rows.length >0)
		{
		var keywords = [[]];
		var delimiter;
		
		if (default_number_of_keywords > results.rows.length){
		delimiter = results.rows.length;
		}
		else { delimiter = default_number_of_keywords; }
		
		for (var i = 0 ; i < delimiter; i ++) {
		keywords[i] = [ results.rows.item(i).KW_ID,results.rows.item(i).KW_WORD, results.rows.item(i).KW_WEIGHT];				
		}				
		
		
		
 level_10 = keywords[0][2];
 level_1 = keywords[keywords.length-1][2];
 
 diff = level_10 - level_1;
 dist = diff/7;
 
 level_9 = 1 + (dist*6);
 level_8 = 1 + (dist*5);
 level_7 = 1 + (dist * 4);
 level_6 = 1 + (dist*3);
 level_5 = 1 + (dist*2);
 level_4 = 1 + dist;
 level_3 = 1 + (dist/2);
 level_2 = 1 + (dist/4);
 
 /*
 alert("level1 "+ level_1);
 alert("level2 "+ level_2);
 alert("level3 "+ level_3);
 alert("level4 "+ level_4);
 alert("level5 "+ level_5);
 alert("level6 "+ level_6);
 alert("level7 "+ level_7);
 alert("level8 "+ level_8);
 alert("level9 "+ level_9);
 alert("level10 "+ level_10);
 */

 //Let's display the tag_cloud trying to order by words ...
 keywords.sort();
 
 for(var i =0; i< keywords.length; i++){
 var size = getTagClass(keywords[i][2]);
 tagClouds+= "<a href=\"#\" onclick=\"searchByKeywordId('"+keywords[i][0]+"',document.getElementById('searchResult'))\" class='"+size+"'>"+keywords[i][1] + "</a> &nbsp;";
 }
 }
  element.innerHTML="<p>"+tagClouds+"</p>";					
		
	});
});
}

function cleanSearch(){
document.getElementById("searchResult").innerHTML="";
document.getElementById("tag_cloud").innerHTML="";
}

function getTagClass(z) {   
  var tagClass = "";  

	if (z == level_10){
	tagClass="level10Tag";
	} else if ( z >= level_9){
	tagClass="level9Tag";
	}
	else if (z >= level_8){
	tagClass="level8Tag";
	}
	else if (z >= level_7){
	tagClass="level7Tag";
	}
	else if (z >= level_6){
	tagClass="level6Tag";
	}
	else if (z >= level_5){
	tagClass="level5Tag";
	}
	else if (z >= level_4){
	tagClass="level4Tag";
	}
	else if (z>= level_3){
	tagClass="level3Tag";
	}
	else if (z>= level_2){
	tagClass="level2Tag";
	}
	else if (z>= level_1){
	tagClass="level1Tag";
	}
	else {
	tagClass="unkwnown";
	}   
  return tagClass;
}
