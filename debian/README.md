## Intro

## Prerequisites

```
sudo apt install debhelper
sudo apt install devscripts
```

## Build

After a maven jar generation, generate a non-signed debian package via:

```bash
debuild -us -uc -b
```
in the parent of this directory. This will generate the deb file in the parent directory of this repository.

You can increase changelog version and comments with `dch` utility, like with `dch -i` that increase minor version.

Add to your .gitignore:
```
*/*.debhelper
*/*.debhelper.log
*.buildinfo
*.substvars
debian/files
debian/ala-namematching-service
```

## Testing

You can test the generated package without install it with `piuparts` like:

```bash
sudo piuparts -D ubuntu -d xenial -d bionic ../ala-namematching-service_1.8-SNAPSHOT_all.deb
```
in this way you can also test it in different releases.

Read `/usr/share/doc/piuparts/README.*` for more usage samples.

## No interactive install

You can preseed this package with something similar to:

```bash
echo "ala-namematching-service ala-namematching-service/source string https://some-tar.url" | debconf-set-selections

export DEBCONF_FRONTEND=noninteractive
apt-get install -yq --force-yes ala-namematching-service
```
useful for Dockerfiles.

## For those without Debian

If you don't have an instance of debian lying about, you can used a suitably
augmented debian docker image to do the build.
To build the image, go to the `server/docker` subdirectory and use the command

```shell
docker build -f Dockerfile-deb-builder . -t deb-builder
```

This will create a version of debian with the required tools installed.
Once you have done this, in the repository root directory, use the following command:

```shell
docker run --rm -v ~/src:/src -w /src/ala-namematching-service deb-builder:latest debuild -us -uc -b
```

Where you replace `~/src` with the parent directory of the repository.
This command will do a complete build from a clean image and
put the resulting debian package in `~/src` or whereever your project resides.