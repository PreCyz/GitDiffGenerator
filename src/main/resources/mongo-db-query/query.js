db.statistics.find({"lastUpdateStatus" : "SUCCESS"})
   .projection({})
   .sort({_id:-1})
   .limit(100);


db.statistics.find({
    "lastExecutionDate": {
        $gte: "2021-06-01T00:00:00.000Z"
    },
    "lastUpdateStatus" : {$in : ["SUCCESS", "PARTIAL_SUCCESS"]}
})
.projection({username : "$username", status : "$lastUpdateStatus", executionDate : "$lastExecutionDate"})
.sort({username:1})
.limit(100);

db.statistics
.find({
    "lastExecutionDate": {
        $gte: "2021-07-01T00:00:00.000Z"
    },
    "lastUpdateStatus" : {$in : ["FAIL"]}
})
.projection({
    username : "$username",
    applicationVersion : "$applicationVersion",
    status : "$lastUpdateStatus",
    executionDate : "$lastExecutionDate",
    exceptions : {
        $filter : {
            input : "$exceptions",
            as : "exception",
            cond: { $gte : ["$$exception.errorDate", "2021-07-01T00:00:00.000Z"] }
        }
    }
})
.sort({username:1})
.limit(100);