<?php
class MyDB extends SQLite3
{
    function __construct()
    {
        $this->open('IoTParking.db');
    }
}

$db = new MyDB();
if(!$db)
{
    echo $db->lastErrorMsg();
}

$hId = $_GET['hId']; //we're getting the passed hId as a paramater in the url

$query = $db->prepare("SELECT * FROM OVERVIEW_3");

$results = $query->execute()->fetchArray();
echo $results;
?>