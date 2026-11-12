package com.clinique.medecins;
import java.net.URI;
import java.net.http.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, properties={
 "spring.datasource.url=jdbc:h2:mem:medecins-tests;DB_CLOSE_DELAY=-1",
 "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("Tests HTTP du service médecins")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MedecinApiTests {
 @Value("${local.server.port}") int port;
 @Autowired MedecinRepository repository;
 @Autowired MedecinPhotoRepository photos;
 final HttpClient client=HttpClient.newHttpClient();
 final String registration="""
 {"nom":"Alami","prenom":"Sara","email":"sara@example.test","motDePasse":"Secret123!","specialite":"Cardiologie"}
 """;
 @BeforeEach void clean() { photos.deleteAll(); repository.deleteAll(); }
 HttpResponse<String> request(String method,String path,String body) throws Exception {
  return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path))
   .header("Content-Type","application/json")
   .method(method,body==null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
   .build(),HttpResponse.BodyHandlers.ofString());
 }
 @Test
 @Tag("rapport-medecins")
 @Order(1)
 @DisplayName("MED 01 - Création, consultation et confidentialité du mot de passe")
 void createsAndReadsWithoutExposingPassword() throws Exception {
  var created=request("POST","/medecins",registration);
  assertThat(created.statusCode()).isEqualTo(201);
  assertThat(created.body()).contains("MEDECIN","Cardiologie").doesNotContain("motDePasse","Secret123!","$2");
  String location=created.headers().firstValue("Location").orElseThrow();
  assertThat(request("GET",location,null).statusCode()).isEqualTo(200);
  var saved=repository.findAll().getFirst();
  assertThat(saved.id).isPositive();
  assertThat(new BCryptPasswordEncoder().matches("Secret123!",saved.motDePasse)).isTrue();
 }
 @Test
 @Tag("rapport-medecins")
 @Order(2)
 @DisplayName("MED 02 - Refus d’un email déjà utilisé sans distinction de casse")
 void rejectsDuplicateEmailCaseInsensitively() throws Exception {
  assertThat(request("POST","/medecins",registration).statusCode()).isEqualTo(201);
  assertThat(request("POST","/medecins",registration.replace("sara@example.test","SARA@example.test")).statusCode()).isEqualTo(409);
  assertThat(repository.count()).isEqualTo(1);
 }
 @Test
 @Tag("rapport-medecins")
 @Order(3)
 @DisplayName("MED 03 - Validation des données et médecin introuvable")
 void validatesInputAndMissingProfiles() throws Exception {
  assertThat(request("POST","/medecins","{}").statusCode()).isEqualTo(400);
  assertThat(request("GET","/medecins/999999",null).statusCode()).isEqualTo(404);
  assertThat(request("GET","/medecins/0",null).statusCode()).isEqualTo(400);
  assertThat(request("POST","/medecins",registration.replace("Secret123!","é".repeat(40))).statusCode()).isEqualTo(400);
 }
 @Test
 @Tag("rapport-medecins")
 @Order(4)
 @DisplayName("MED 04 - Filtrage et modification sans changer le mot de passe")
 void filtersAndUpdatesProfileWithoutChangingPassword() throws Exception {
  var created=request("POST","/medecins",registration);
  String location=created.headers().firstValue("Location").orElseThrow();
  String hash=repository.findAll().getFirst().motDePasse;
  assertThat(request("GET","/medecins?specialite=cardiologie",null).body()).contains("sara@example.test");
  assertThat(request("GET","/medecins?specialite=Neurologie",null).body()).isEqualTo("[]");
  var updated=request("PUT",location,"""
  {"nom":"Alami","prenom":"Sara","email":"updated@example.test","specialite":"Neurologie"}
  """);
  assertThat(updated.statusCode()).isEqualTo(200);
  assertThat(updated.body()).contains("Neurologie","updated@example.test").doesNotContain("motDePasse");
  assertThat(repository.findAll().getFirst().motDePasse).isEqualTo(hash);
 }
 @Test
 @Tag("rapport-medecins")
 @Order(5)
 @DisplayName("MED 05 - Refus d’un email en conflit à la modification")
 void refusesUpdateToAnotherDoctorsEmail() throws Exception {
  request("POST","/medecins",registration);
  var other=request("POST","/medecins",registration.replace("sara@example.test","other@example.test"));
  var update=request("PUT",other.headers().firstValue("Location").orElseThrow(),"""
  {"nom":"Other","prenom":"Doctor","email":"sara@example.test","specialite":"Neurologie"}
  """);
  assertThat(update.statusCode()).isEqualTo(409);
 }

 @Test
 @Tag("rapport-medecins")
 @Order(6)
 @DisplayName("MED 06 - Envoi, consultation et remplacement de la photo")
 void uploadsReadsAndReplacesPhotos() throws Exception {
  String path=request("POST","/medecins",registration).headers().firstValue("Location").orElseThrow()+"/photo";
  assertThat(request("GET",path,null).statusCode()).isEqualTo(404);
  assertThat(upload(path,image("png",10,10),"image/png").statusCode()).isEqualTo(204);
  var download=client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path)).build(),HttpResponse.BodyHandlers.ofByteArray());
  assertThat(download.statusCode()).isEqualTo(200);
  assertThat(download.headers().firstValue("Content-Type")).contains("image/png");
  assertThat(download.headers().firstValue("X-Content-Type-Options")).contains("nosniff");
  assertThat(javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(download.body())).getWidth()).isEqualTo(10);
  assertThat(upload(path,image("jpg",20,20),"image/jpeg").statusCode()).isEqualTo(204);
  assertThat(photos.count()).isEqualTo(1);
  assertThat(request("GET",path,null).headers().firstValue("Content-Type")).contains("image/jpeg");
  assertThat(request("GET",path.replace("/photo",""),null).body()).doesNotContain("imageData","motDePasse");
 }
 @Test
 @Tag("rapport-medecins")
 @Order(7)
 @DisplayName("MED 07 - Refus des photos invalides sans écraser la précédente")
 void rejectsInvalidOrOversizedImagesWithoutOverwritingExistingPhoto() throws Exception {
  String path=request("POST","/medecins",registration).headers().firstValue("Location").orElseThrow()+"/photo";
  upload(path,image("png",10,10),"image/png");
  assertThat(upload(path,"<script>alert(1)</script>".getBytes(),"image/png").statusCode()).isEqualTo(400);
  assertThat(upload(path,new byte[0],"image/png").statusCode()).isEqualTo(400);
  assertThat(upload(path,image("gif",10,10),"image/png").statusCode()).isEqualTo(400);
  assertThat(upload(path,image("png",4097,1),"image/png").statusCode()).isEqualTo(400);
  assertThat(upload(path,new byte[2*1024*1024+1],"image/png").statusCode()).isEqualTo(413);
  assertThat(photos.findAll().getFirst().contentType).isEqualTo("image/png");
  assertThat(upload("/medecins/999999/photo",image("png",10,10),"image/png").statusCode()).isEqualTo(404);
 }
 @Test
 @Order(8)
 @DisplayName("Complément - Nettoyage des images et erreurs JSON")
 void stripsTrailingContentAndReturnsJsonForBadRequests() throws Exception {
  String path=request("POST","/medecins",registration).headers().firstValue("Location").orElseThrow()+"/photo";
  var bytes=new java.io.ByteArrayOutputStream();
  bytes.write(image("png",10,10)); bytes.write("TRAILING_PAYLOAD".getBytes());
  assertThat(upload(path,bytes.toByteArray(),"image/png").statusCode()).isEqualTo(204);
  assertThat(new String(photos.findAll().getFirst().imageData,java.nio.charset.StandardCharsets.ISO_8859_1)).doesNotContain("TRAILING_PAYLOAD");
  var badId=request("GET","/medecins/no-id",null);
  assertThat(badId.statusCode()).isEqualTo(400);
  assertThat(badId.body()).contains("message");
  var malformed=request("POST","/medecins","{invalid");
  assertThat(malformed.statusCode()).isEqualTo(400);
  assertThat(malformed.body()).contains("message").doesNotContain("stackTrace");
 }
 private byte[] image(String format,int width,int height) throws Exception {
  var image=new java.awt.image.BufferedImage(width,height,java.awt.image.BufferedImage.TYPE_INT_RGB);
  var output=new java.io.ByteArrayOutputStream();
  javax.imageio.ImageIO.write(image,format,output);
  return output.toByteArray();
 }
 private HttpResponse<String> upload(String path,byte[] data,String type) throws Exception {
  String boundary="clinic-test-boundary";
  var output=new java.io.ByteArrayOutputStream();
  output.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\"file\"; filename=\"photo.png\"\r\nContent-Type: "+type+"\r\n\r\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
  output.write(data); output.write(("\r\n--"+boundary+"--\r\n").getBytes());
  return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path))
   .header("Content-Type","multipart/form-data; boundary="+boundary)
   .PUT(HttpRequest.BodyPublishers.ofByteArray(output.toByteArray())).build(),HttpResponse.BodyHandlers.ofString());
 }
}
