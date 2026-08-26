# CareRoute frontend

This is a self-contained browser interface for the Hospital Smart Service Operations Optimizer.

Build and launch the actual application server from the repository root:

```bash
./build.sh
java -cp "lib/sqlite-jdbc.jar:build/app:shared/src/main/resources" com.hospitaloptimizer.pod1.WebServer
```

Then open `http://127.0.0.1:8091/` in a browser. On Windows Git Bash, the command is the same; on PowerShell, replace the classpath colons with semicolons. Pass a port number at the end (for example, `WebServer 8092`) when `8091` is already occupied.

The interface reads and updates the SQLite-backed application data through a local-only API. Creating requests and assigning resources write to the database and audit log; route searches execute the project's Dijkstra implementation.
