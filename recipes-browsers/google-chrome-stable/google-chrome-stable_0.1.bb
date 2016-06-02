SUMMARY = "Google Chrome"
DESCRIPTION = "Google Chrome Latest Stable Stub installer"

# This package implements a stub installer it requires outside network
# access in order to succeed at install time.  

# The DEMO_BROWSER_CACHE_INSTALL variable will allow for the creation
# of an offline install package only for the purpose of a
# demonstration without full network access.  This is not intended for
# release purposes.

DEMO_BROWSER_CACHE_INSTALL ?= "0"

RDEPENDS_${PN} = "bash xdg-utils libexif cairo freetype libxcursor libxtst nss libxi libxscrnsaver fontconfig gdk-pixbuf libxdamage libxext libxrandr libasound dbus-lib glib-2.0 gtk+ gconf pango libxfixes nspr libx11 atk libxcomposite libxrender cups-lib xz"

LICENSE = "Proprietary"
LIC_FILES_CHKSUM = "file://../eula_text.html;md5=da54da9383ddffa44c5db45513239da2"

FILES_${PN} += "/opt/google/chrome/* /usr/share/*"

# The eual_txt comes from chrome://terms OR
#   https://www.google.com/chrome/browser/privacy/eula_text.html

SRC_URI = "file://eula_text.html"

INSANE_SKIP_${PN} = 'already-stripped ldflags'
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
COMPATIBLE_MACHINE = "(intel-corei7-64|qemux86-64)"

DL_URL = "https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb"

do_install() {
	install -d ${D}/usr/bin
	cat<<EOF>${D}/usr/bin/google-chrome
#!/bin/bash
id=\`id -u\`
if [ "\$id" = 0 ] ; then
   exec /usr/bin/google-chrome-stable --user-data-dir "\$@"
fi
exec /usr/bin/google-chrome-stable "\$@"
EOF
	chmod 755 ${D}/usr/bin/google-chrome
	
	if [ "${DEMO_BROWSER_CACHE_INSTALL}" = 1 ]; then
		if [ "${BB_NO_NETWORK}" = 1 ] ; then
			echo "ERROR: BB_NO_NETWORK must be '0' in order to cache"
			exit 1;
		fi
		if [ ! -f google-chrome-stable_current_amd64.deb ] ; then
			wget "${DL_URL}"
		fi
		ar p google-chrome-stable_current_amd64.deb data.tar.xz | unxz | tar -C ${D} -xf - ./usr ./opt
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
    ar p google-chrome-stable_current_amd64.deb data.tar.xz | unxz | tar -C / -xf - ./usr ./opt
}

pkg_postrm_${PN} () {
    rm -rf /opt/google/chrome \
	/usr/bin/google-chrome-stable \
	/usr/share/menu/google-chrome.menu \
	/usr/share/gnome-control-center/default-apps/google-chrome.xml \
	/usr/share/doc/google-chrome-stable/changelog.gz \
	/usr/share/applications/google-chrome.desktop \
	/usr/share/man/man1/google-chrome.1
    rmdir /opt/google || /bin/true
}
