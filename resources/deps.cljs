{:foreign-libs [{:file     "vendor-js/firebase/firebase-app.js"
                 :provides ["vendor.firebase.app"]}
                {:file     "vendor-js/firebase/firebase-auth.js"
                 :requires ["vendor.firebase.app"]
                 :provides ["vendor.firebase.auth"]}
                {:file     "vendor-js/firebase/firebase-database.js"
                 :requires ["vendor.firebase.app"]
                 :provides ["vendor.firebase.database"]}
                {:file     "vendor-js/firebase/firebase-storage.js"
                 :requires ["vendor.firebase.app"]
                 :provides ["vendor.firebase.storage"]}]
 :externs      ["vendor-js/firebase/firebase-app-externs.js"
                "vendor-js/firebase/firebase-auth-externs.js"
                "vendor-js/firebase/firebase-database-externs.js"
                "vendor-js/firebase/firebase-storage-externs.js"]}