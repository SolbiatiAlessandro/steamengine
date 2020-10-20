# steamengine

A physics engine in clojurescript

## Overview

Features

- hydrodyanmics simulations
- VR compatibility
- served in Python, written in ClojureScript

## Clojure Setup

To get an interactive development environment run:

    npm install
    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).

To run tests (you need to have `node` installed)

    lein doo node test

## Python Setup

With `figwheel` running, set up your venv with `flask` and run 

	python python-server/server.py

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
