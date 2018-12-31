<?php
	$server = 'sql01.backboneservers.com';
	$username = 'phpLolAdmin.myr-tools.ca';
	$password = 'Cassie1234';
	$dbname = 'MyrToolsBackendDatabase_myr-tools_ca';

	$conn = mysqli_connect($server, $username, $password, $dbname);
	mysqli_query($conn, "SET `time_zone` = '-8:00'");
?>