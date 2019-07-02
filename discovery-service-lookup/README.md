Receive : 
{"message_type":"UPP","ID":"IdAssigned"}

Return
* Signature
* FT
* RT
* BC1 to BCn

To get
* Signature: g.V().has("IdAssigned", HASH_UPP).bothE().bothV().hasLabel("signature").values("IdAssigned")
* FT: g.V().has("IdAssigned", HASH_UPP).bothE().bothV().hasLabel("foundation_tree").values("IdAssigned"), save result as **HASH_FT**
* RT: g.V().has("IdAssigned", HASH_FT).bothE().bothV().hasLabel("root_tree").values("IdAssigned"), save result as **HASH_RT**
* BC: g.V().has("IdAssigned, HASH_RT).bothE().bothV().hasLabel("blockchain_IOTA").values("IdAssigned")