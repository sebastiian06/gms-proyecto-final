# Verificación de Requisitos No Funcionales (RNF) — GMS

Este documento detalla cómo se verifica cada uno de los 3 RNF definidos en `docs/requisitos.md`.

---

## RNF-01 — Rendimiento (≤ 2 segundos en el 95% de los casos)

### Descripción medible
Las consultas de listado de clientes, planes y membresías deben responder en **≤ 2 segundos** en el 95% de los casos, con hasta 100 registros por página.

### Forma de verificación

**Herramienta:** `curl` o JMeter (se usará `curl` por simplicidad, midiendo el tiempo total con `Measure-Command` en PowerShell).

**Prerrequisitos:**
- Base de datos con al menos 100 registros en cada tabla (se puede generar con el seed o con un script de inserción masiva).
- API en ejecución (`docker compose up -d`).

**Comandos de medición:**