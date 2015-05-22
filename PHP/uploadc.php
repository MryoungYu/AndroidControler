<?php
include 'conMysql.php';												//连接数据库
$res0 = mysql_query('select * from contacts');						
$num = 1;
$imei = $_POST['id'];
if($res0 != null)
	while( $row = mysql_fetch_array($res0) )
		$num = $num + 1;											//查询数据库计算条目个数，为新增条目编号
for($i=0;$i<$_POST["num"];$i = $i + 1)
{
	$res = mysql_query(												//将上传的数据依次加入数据库
	'insert into contacts VALUES('.$num.'," '.$_POST["name".$i].
	'"," '.$_POST["num".$i].'"," '.$imei.'")'
	);
    $num = $num + 1;
}
?>