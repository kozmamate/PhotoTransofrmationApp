# Photo Transformation App

## Leírás

A Photo Transformation App egy Spring Boot alapú alkalmazás, amely képek feltöltését, átméretezését és biztonságos tárolását teszi lehetővé. Az alkalmazás REST API végpontokat biztosít több kép egyidejű feltöltéséhez, automatikus átméretezéshez és AES titkosítással védett adatbázis tároláshoz.

## Főbb funkciók

- **Többszörös képfeltöltés**: Egyszerre több kép feltöltése REST API-n keresztül
- **Formátum validáció**: Csak PNG és JPG formátumok elfogadása
- **Méret korlátozás**: Maximum 5000x5000 pixel méretkorlátozás
- **Automatikus átméretezés**: Konfigurálható maximum szélesség és magasság paraméterekkel
- **ImageMagick integráció**: Külső képfeldolgozó alkalmazás használata
- **AES titkosítás**: Képek biztonságos tárolása az adatbázisban
- **Metaadat kezelés**: Részletes képinformációk tárolása és lekérdezése

## Technológiai stack

- **Java 17**
- **Spring Boot 3.1.5**
- **Spring Data JPA**
- **Spring Security** (AES titkosításhoz)
- **H2 Database** (fejlesztési környezethez)
- **ImageMagick** (képfeldolgozáshoz)
- **Maven** (build tool)

## Előfeltételek

1. **Java 17** vagy újabb
2. **Maven 3.6+**
3. **ImageMagick** telepítése (opcionális, beépített Java fallback elérhető)

## Konfiguráció

Az alkalmazás konfigurációja az `src/main/resources/application.yml` fájlban található:

```yaml
photo:
  resize:
    max-width: 1920      # Maximum szélesség (opcionális)
    max-height: 1080     # Maximum magasság (opcionális)
  upload:
    max-size: 5000       # Maximum pixel méret (5000x5000)
    allowed-formats: png,jpg,jpeg # Engedélyezett formátumok
  storage:
    path: ./storage
  imagemagick:
    path: "C:/Program Files/ImageMagick-7.1.1-Q16-HDRI/magick.exe"
```

## Futtatás

1. **Projekt klónozása és függőségek telepítése:**
   ```bash
   mvn clean install
   ```

2. **Alkalmazás indítása:**
   ```bash
   mvn spring-boot:run # Vagy SpringDashboard-on az application futtatása (én VSCode-ban használok spring dashboard-ot)
   ```

3. **Alkalmazás elérhető lesz:**
   - **Swagger UI**: http://localhost:8080/swagger-ui.html
   - **OpenAPI Docs**: http://localhost:8080/v3/api-docs
   - H2 Console: http://localhost:8080/h2-console

## API Dokumentáció

### Swagger UI
Az API teljes dokumentációja és interaktív tesztelési felülete elérhető a **Swagger UI** segítségével:

🔗 **http://localhost:8080/swagger-ui.html**

A Swagger UI-ban:
- ✅ Minden endpoint dokumentálva van
- ✅ Interaktív tesztelés lehetséges
- ✅ Példa válaszok és hibakódok
- ✅ Fájlfeltöltés tesztelése közvetlenül a felületen

### OpenAPI JSON dokumentáció
- 📄 **http://localhost:8080/v3/api-docs**

## API Végpontok

### Képfeltöltés
```http
POST /api/files/upload
Content-Type: multipart/form-data

files: [fájlok]
```

**Válasz:**
```json
{
  "success": true,
  "message": "Successfully uploaded 2 photo(s)",
  "totalUploaded": 2,
  "uploadedPhotos": [
    {
      "id": 1,
      "originalFileName": "photo.jpg",
      "fileName": "uuid-generated-name.jpg",
      "contentType": "image/jpeg",
      "fileSize": 2048576,
      "originalWidth": 3000,
      "originalHeight": 2000,
      "resizedWidth": 1920,
      "resizedHeight": 1280,
      "uploadedAt": "2025-10-01T10:30:00",
      "processedAt": "2025-10-01T10:30:01",
      "isProcessed": true
    }
  ]
}
```

### Összes kép metaadatainak lekérdezése
```http
GET /api/files
```

### Kép metaadatainak lekérdezése ID alapján
```http
GET /api/files/{id}/metadata
```

### Kép letöltése
```http
GET /api/files/{id}/download
```

## Biztonsági funkciók

- **AES Titkosítás**: Minden feltöltött kép AES algoritmussal titkosítva van tárolva
- **Kulcs generálás**: A `SecretKeyGenerator` utility osztály automatikusan generál és ment egy titkos kulcsot
- **Fájl validáció**: Csak engedélyezett formátumok és méretek elfogadása
- **CORS beállítás**: Konfigurálható cross-origin hozzáférés

## Fejlesztési információk

### Projektstruktúra
```
src/main/java/com/phototransformation/
├── PhotoTransformationApplication.java   # Spring Boot indító osztály
├── config/                               # Konfigurációs osztályok
├── controller/                           # REST kontrollerek és kivételkezelés
│   └── service/
├── dto/                                  # Data Transfer Objects
├── entity/                               # JPA entitások
├── mapper/                               # Entitás ↔ DTO konverziók
├── repository/                           # Data Access Layer (Spring Data JPA)
├── service/                              # Üzleti logika (PhotoManagerService)
└── util/                                 # Segédosztályok (titkosítás, képfeldolgozás, kulcsgenerálás)

src/test/java/com/phototransformation/
├── controller/service/ 
├── service/                 
└── util/                                 # ImageProcessingUtil unit tesztek
```

### Kulcs fájl kezelése

A titkosítási kulcs automatikusan generálódik és a `secretKey.key` fájlba mentődik a projekt gyökérkönyvtárába. Ez a fájl **nem** kerül verziókezelésbe biztonsági okokból.

## Tesztelés

Az alkalmazás tartalmaz unit teszteket. Futtatás:

```bash
mvn test
```
Vagy a teljes buildeléssel egyetemben

```bash
mvn clean install
```

## Hibaelhárítás

### ImageMagick problémák
Ha ImageMagick nem elérhető, az alkalmazás automatikusan a beépített Java képfeldolgozást használja.

### Memória problémák nagy képeknél
Állítsd be a JVM heap méretet:
```bash
java -Xmx2g -jar photo-transformation-app.jar
```

## Licenc

Ez a projekt felvételi feladat alapján készült és az ott végbemenő értékelsi folyamat során használható.