#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p bin
find src -name '*.java' -print > fuentes.txt
trap 'rm -f fuentes.txt' EXIT
javac -encoding UTF-8 -Xlint:all -d bin @fuentes.txt
case "${1:-consola}" in
  pruebas)
    javac -encoding UTF-8 -cp bin -d bin srctest/pruebas/DatosPrueba.java srctest/pruebas/PruebasSistema.java srctest/pruebas/PruebasRevision.java
    java -cp bin pruebas.PruebasSistema
    ;;
  datos)
    javac -encoding UTF-8 -cp bin -d bin srctest/pruebas/DatosPrueba.java
    java -cp bin pruebas.DatosPrueba datos/club-ejemplo.json
    ;;
  consola) java -cp bin consola.ConsolaPrincipal ;;
  socio) java -cp bin consola.AplicacionSocio ;;
  empleado) java -cp bin consola.AplicacionEmpleado ;;
  administrador) java -cp bin consola.AplicacionAdministrador ;;
  *) echo "Uso: sh ejecutar.sh [pruebas|datos|consola|administrador|socio|empleado]"; exit 1 ;;
esac
