

stock-docker:
	mvn -pl stock/market -am clean compile && cd stock/market && mvn jib:dockerBuild -Dimage="test-market"