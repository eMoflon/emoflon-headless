#!/bin/bash

set -e
START_PWD=$PWD


#
# Config
#

ECLIPSE_ARCHIVE=eclipse-emoflon-linux-user-ci

#
# Utils
#

# Displays the given input including "=> " on the console.
log () {
	echo "=> $1"
}

# Cleans directories from previous runs up
cleanup () {
    rm -rf ./eclipse
    rm -rf ./repo
    rm -rf ./ws

    mkdir -p ./repo
    mkdir -p ./ws
}

#
# Script
#

# Clean
log "Clean-up."
cleanup

# Get eclipse
if [[ ! -f "./$ECLIPSE_ARCHIVE.zip" ]]; then
	log "Downloading latest eMoflon Eclipse archive from Github."
	curl -s https://api.github.com/repos/maxkratz/emoflon-eclipse-build/releases/latest \
        | grep "$ECLIPSE_ARCHIVE.*zip" \
        | cut -d : -f 2,3 \
        | tr -d \" \
        | wget -qi -
fi

log "Unzip new Eclipse from archive."
unzip -qq -o $ECLIPSE_ARCHIVE.zip

# Get repo
log "Clone repository"
cd repo
git clone git@github.com:eMoflon/emoflon-headless.git

# Import projects
log "Import projects into the Eclipse workspace."
cd $START_PWD/eclipse
./eclipse -noSplash -consoleLog -data $START_PWD/ws -application com.seeq.eclipse.importprojects.headlessimport -importProject $START_PWD/repo/emoflon-headless

# Build projects
log "Build all projects."
xvfb-run --auto-servernum --server-args="-ac" ./eclipse -noSplash -consoleLog -data $START_PWD/ws -application org.eclipse.jdt.apt.core.aptBuild

# Trigger SiteXML application to build the updatesite
log "Invoke SiteXML application."
xvfb-run --auto-servernum --server-args="-ac" ./eclipse -noSplash -consoleLog -data $START_PWD/ws -application org.emoflon.site.build.SiteXMLBuilder -project org.emoflon.headless.updatesite

# Invoke ant to create updatesite.zip
log "Run ant script."
cd $START_PWD/repo/emoflon-headless/org.emoflon.headless.updatesite
ant -f AntUtilitiesBuild.xml createArchive
