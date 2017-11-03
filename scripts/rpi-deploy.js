var JSFtp = require("jsftp");

var ftp = new JSFtp({
    host: "10.0.0.99",
    port: 48159,
    user: "gavan",
    pass: "stablade"
});

ftp.ls(".", function(err, res) {
    console.log(res);
});