<?php
    function post() {
        global $conn;
        global $fn;

        $sql = "INSERT INTO Times(Function) VALUES ('$fn')";

        mysqli_query($conn, $sql);
    }

    function listFn(){
        global $conn;

        $sql = "SELECT TimeID,Time,Function FROM Times";

        $result = mysqli_query($conn, $sql);

        $dataF = array();
		$count = 0;

        while($row = mysqli_fetch_assoc($result)) {
			$data = array();
            preg_match('/(\d{4})-(\d{2})-(\d{2})\s(\d{2}):(\d{2}):(\d{2})/', $row['Time'], $match);
    		$year = (int) $match[1];
    		$month = (int) $match[2]; // convert to zero-index to match javascript's dates
    		$day = (int) $match[3];
    		$hours = (int) $match[4];
    		$minutes = (int) $match[5];
    		$seconds = (int) $match[6];
			$id = $row['TimeID'];
			$data['Date'] = "Date($year, $month, $day, $hours, $minutes, $seconds)";
			$data['Function'] = $row['Function'];
			$dataF[$count] = $data;
			$count++;
        }

        return $dataF;
    }
?>