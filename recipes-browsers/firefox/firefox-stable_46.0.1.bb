SUMMARY = "Firefox Stub Installer"
DESCRIPTION = "Firefox Latest Stable Stub installer"

# This package implements a stub installer it requires outside network
# access in order to succeed at install time.  

# The DEMO_BROWSER_CACHE_INSTALL variable will allow for the creation
# of an offline install package only for the purpose of a
# demonstration without full network access.  This is not intended for
# release purposes.

DEMO_BROWSER_CACHE_INSTALL ?= "0"

RDEPENDS_${PN} = "bash cairo freetype gtk+3 nss dbus-glib fontconfig gdk-pixbuf libxt libxdamage libxext cairo-gobject libasound dbus-lib glib-2.0 gtk+ pango libxfixes nspr libx11 atk libxcomposite libxrender"

LICENSE = "MPL2.0"
LIC_FILES_CHKSUM = "file://../license.html;md5=58d683395c01f85be8652139e81d5c12"

FILES_${PN} += "/opt/*"

# The license.html file comes from about:license in Firefox

SRC_URI = "file://license.html"
DL_URL = "https://download.mozilla.org/?product=firefox-${PV}-SSL&os=linux64&lang=en-US"

INSANE_SKIP_${PN} = 'already-stripped ldflags'
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
COMPATIBLE_MACHINE = "(intel-corei7-64|qemux86-64)"

do_install() {
	install -d ${D}/usr/bin
	cat<<EOF>${D}/usr/bin/firefox
#!/bin/bash
exec /opt/firefox/firefox "\$@"
EOF
	chmod 755 ${D}/usr/bin/firefox
	
	if [ "${DEMO_BROWSER_CACHE_INSTALL}" = 1 ]; then
		if [ "${BB_NO_NETWORK}" = 1 ] ; then
			echo "ERROR: BB_NO_NETWORK must be '0' in order to cache"
			exit 1;
		fi
		if [ ! -f "index.html?product=firefox-${PV}-SSL&os=linux64&lang=en-US" ] ; then
			wget "${DL_URL}"
		fi
		install -d ${D}/opt
		tar -C ${D}/opt -xf "index.html?product=firefox-${PV}-SSL&os=linux64&lang=en-US"
	fi
}

pkg_preinst_${PN} () {
    #!/bin/sh -e
    if [ x"$D" != "x" ]; then
        echo "Cross install not supported"
        exit 1
    fi
    if [ "${DEMO_BROWSER_CACHE_INSTALL}" = 1 ]; then
        exit 0
    fi
    dl=`mktemp -d`
    cd $dl
    wget "${DL_URL}"
    install -d /opt
    tar -C /opt -xf "index.html?product=firefox-${PV}-SSL&os=linux64&lang=en-US"
}

pkg_postrm_${PN} () {
    rm -rf /opt/firefox /usr/bin/firefox
}
