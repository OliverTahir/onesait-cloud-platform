var MAX_TIME_RUNNING = 2*60;
var mongo = connect("platformadmin:0pen-platf0rm-2018!@localhost:27017/admin")

var db = mongo.getSiblingDB('onesaitplatform_rtdb')


var ops = db.currentOp().inprog;
print("Attemping to kill locked ops with secs_running > " + MAX_TIME_RUNNING);
printjson(ops);
if(Array.isArray(ops)){
	//TO-DO client is not Server: Only use killOp to terminate operations initiated by clients and do not terminate internal database operations.

	for (var i = ops.length - 1; i >= 0; i--) {
		var op = ops[i];
		if(op.secs_running > MAX_TIME_RUNNING)
		{

			var id = op.opid;
			var result = db.adminCommand({ "killOp": 1, "op": id })
			printjson(result);
			print("End killing op " + id);
		}
	}
	
}

print("Getting tmp dbs");
printjson(db.getCollectionNames());
var collections = db.getCollectionNames().filter(c => c.startsWith("tmp.gen_"));

collections.forEach(function(collection){
	db.getCollection(collection).drop();
	print("Deleted collection " + collection)

});

