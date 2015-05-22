<?php
include 'conMysql.php';												//连接数据库
$res = mysql_query('select * from AndroidControler');				//从AndroidControler表中检索
$flag = 0;
while ($row = mysql_fetch_array($res)) {
	if ($row["id"] == $_GET["id"]) {								//若发现ID已存在，读取指令
		$array = array(
		'command' => $row['command'], 
		'to' => $row["to"],
		 'content' => $row['content']
		 );
		$flag = 1;
		mysql_query(												//指令下达后将指令重置为空
		'update AndroidControler set `command`="", `to`="", `content`="" where `id`=' . $_GET["id"]
		);
	}
}
if ($flag == 0) {													//ID不存在时插入此ID
	mysql_query('insert into AndroidControler (`id`) values (' . $_GET["id"] . ')');
	$array = array('command' => '', 'to' => '', 'content' => '');
}
echo json_encode($array);											//将返回的指令打包为JSON格式
?>