#!/bin/sh
# -----------------------------------------------------------------------------
# Start Script for the UNICORE 'OGC Web Processing Service' Simple Job Executor
# -----------------------------------------------------------------------------

# build the classpath
# for FILE in ./lib/*.jar; do
for FILE in ~/*.jar; do
	CLASSPATH="${CLASSPATH:+${CLASSPATH}:}$FILE"
done

echo $CLASSPATH

# run the job executor application
java -Xmx256m -Xms256m -classpath "$CLASSPATH" org.n52.wps.grid.client.unicore6.Unicore6Executor
