<?php
include 'conMysql.php';
$res0 = mysql_query('select * from messages');
$num = 1;
$imei = $_POST['id'];
if ($res0 != null)
	while ($row = mysql_fetch_array($res0))
		$num = $num + 1;
for ($i = 0; $i < $_POST["num"]; $i = $i + 1) {
	$res = mysql_query(
	'insert into messages VALUES(' . $num . '," ' 
	. $_POST["number" . $i] . '"," ' . $_POST["body" . $i] 
	. '"," ' . $imei . '","' . $_POST["type" . $i] . '")');
	$num = $num + 1;
}
?>