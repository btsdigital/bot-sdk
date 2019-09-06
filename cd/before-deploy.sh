#!/usr/bin/env bash
if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    openssl aes-256-cbc -K $encrypted_c386e241d56c_key -iv $encrypted_c386e241d56c_iv -in cd/codesigning.asc.enc -out cd/codesigning.asc -d
fi