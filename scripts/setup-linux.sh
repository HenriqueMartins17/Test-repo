#!/bin/bash
# setup local development envinroment for linux

sdk env install
nvm install 16.15.0 && nvm use 16.15.0
# shellcheck source=/dev/null
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- --profile minimal -y && source "$HOME/.cargo/env"