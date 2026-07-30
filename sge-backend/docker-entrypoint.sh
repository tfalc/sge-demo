#!/bin/sh
set -eu

# Render injeta DATABASE_URL como postgres://... ; Spring espera jdbc:postgresql://...
if [ -n "${DATABASE_URL:-}" ] && [ -z "${SPRING_DATASOURCE_URL:-}" ]; then
  url="$DATABASE_URL"
  # Remove query string (sslmode etc.) — reaplicamos ssl abaixo se preciso
  bare="${url%%\?*}"
  query=""
  case "$url" in
    *\?*) query="${url#*\?}" ;;
  esac

  case "$bare" in
    postgres://*|postgresql://*)
      rest="${bare#*://}"
      userinfo="${rest%%@*}"
      hostpath="${rest#*@}"
      user="${userinfo%%:*}"
      pass="${userinfo#*:}"
      export SPRING_DATASOURCE_USERNAME="$user"
      export SPRING_DATASOURCE_PASSWORD="$pass"
      if [ -n "$query" ]; then
        export SPRING_DATASOURCE_URL="jdbc:postgresql://${hostpath}?${query}"
      else
        # Render Postgres exige SSL
        export SPRING_DATASOURCE_URL="jdbc:postgresql://${hostpath}?sslmode=require"
      fi
      ;;
  esac
fi

exec java -jar app.jar
