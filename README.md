[![Coverage Status](https://coveralls.io/repos/github/Max-Meldrum/SpaceTurtle/badge.svg)](https://coveralls.io/github/Max-Meldrum/SpaceTurtle)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://travis-ci.org/Max-Meldrum/SpaceTurtle.svg?branch=master)](https://travis-ci.org/Max-Meldrum/SpaceTurtle)

# SpaceTurtle

Development ongoing.


Set up ZooKeeper for development
```
$ ./docker/zookeeper_setup.sh
```

Compile SpaceTurtle
```
$ ./compile.sh
```

Run SpaceTurtle 
```
$ ./bin/spaceTurtle.sh
```

Run SpaceTurtle command line client
```
$ ./bin/spaceTurtleCli.sh
```

Run the tests with enabled coverage
```
$ sbt clean coverage test
```

To generate the coverage reports run
```
$ sbt coverageReport
```


# Tasks

- [x] Join cluster through ZooKeeper
- [x] Set up auth to ZooKeeper
- [ ] Set up SSL with Netty
- [ ] Transfer files to agents







