#!/usr/bin/env bash
set -euo pipefail

get_token() {
  # Get the bearer token in exchange for the service account credentials.
  local service_account_key_file_path="${1}"
  local iap_client_id="${2}"

  local iam_scope="https://www.googleapis.com/auth/iam"
  local oauth_token_uri="https://www.googleapis.com/oauth2/v4/token"

  local private_key_id="$(cat "${service_account_key_file_path}" | jq -r '.private_key_id')"
  local client_email="$(cat "${service_account_key_file_path}" | jq -r '.client_email')"
  local private_key="$(cat "${service_account_key_file_path}" | jq -r '.private_key')"
  local issued_at="$(date +%s)"
  local expires_at="$((issued_at + 3600))"
  local header="{'alg':'RS256','typ':'JWT','kid':'${private_key_id}'}"
  local header_base64="$(echo "${header}" | base64)"
  local payload="{'iss':'${client_email}','aud':'${oauth_token_uri}','exp':${expires_at},'iat':${issued_at},'sub':'${client_email}','target_audience':'${iap_client_id}'}"
  local payload_base64="$(echo "${payload}" | base64)"
  local signature_base64="$(printf %s "${header_base64}.${payload_base64}" | openssl dgst -binary -sha256 -sign <(printf '%s\n' "${private_key}")  | base64)"
  local assertion="${header_base64}.${payload_base64}.${signature_base64}"
  local token_payload="$(curl -s \
    --data-urlencode "grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer" \
    --data-urlencode "assertion=${assertion}" \
    https://www.googleapis.com/oauth2/v4/token)"
  local bearer_id_token="$(echo "${token_payload}" | jq -r '.id_token')"
  echo "${bearer_id_token}"
}

main(){
  # TODO: Replace the below variables.
  SERVICE_ACCOUNT_KEY="service_account_key_file_path"
  IAP_CLIENT_ID="iap_client_id"
  URL="application_url"

  # Obtain the Bearer ID token.
  ID_TOKEN=$(get_token "${SERVICE_ACCOUNT_KEY}" "${IAP_CLIENT_ID}")
  # Access the application with the Bearer ID token.
  curl --header "Authorization: Bearer ${ID_TOKEN}" "${URL}"
}

main "$@"
