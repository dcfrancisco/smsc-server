UPDATE SMSC_MESSAGE SET
	datacoding = {datacoding},
	defaultmessage = {defaultmessage},
	destaddr = {destaddr},
	destaddrnpi = {destaddrnpi},
	destaddrton = {destaddrton},
	esmclass = {esmclass},
	messageLength = {messageLength},
	nexttrydelivertime = {nexttrydelivertime},
	priorityflag = {priorityflag},
	protocolid = {protocolid},
	received = {received},
	replacedby = {replacedby},
	replaced = {replaced},
	scheduledate = {scheduledate},
	servicetype = {servicetype},
	shortmessage = {shortmessage},
	sourceaddr = {sourceaddr},
	sourceaddrnpi = {sourceaddrnpi},
	sourceaddrton = {sourceaddrton},
	status = {status},
	validityperiod = {validityperiod}
WHERE id = {id};