db.statistics.find({
    lastExecutionDate : {$gt : "2021-05-01T00:00:00.000"},
    lastUpdateStatus : {$in : ["SUCCESS", "PARTIAL_SUCCESS"]}
})
   .projection({})
   .sort({_id:-1});
   
db.statistics.find({
    lastExecutionDate : {$gt : "2021-05-01T00:00:00.000"},
    lastUpdateStatus : "FAIL"})
   .projection({})
   .sort({_id:-1});
   
db.statistics.aggregate({lastExecutionDate : {$gt : "2021-01-01T00:00:00.000"}})
   .projection({user : "$username", lastExecutionDate : "$lastExecutionDate"})
   .sort({user:1});
   
db.statistics.find({username : "LUFU"})
   .projection({})
   .sort({_id:-1})