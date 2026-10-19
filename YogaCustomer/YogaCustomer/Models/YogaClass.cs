using Google.Cloud.Firestore;

namespace YogaCustomer.Models
{
    [FirestoreData]
    public class YogaClass
    {
        [FirestoreProperty]
        public int Id { get; set; }

        [FirestoreProperty]
        public int CourseId { get; set; }

        [FirestoreProperty]
        public string Teacher { get; set; }

        [FirestoreProperty]
        public string Date { get; set; } // yyyy-mm-dd format

        [FirestoreProperty]
        public string? Comments { get; set; }

        [FirestoreProperty]
        public string CourseName { get; set; }

        [FirestoreProperty]
        public double Price { get; set; }

        [FirestoreProperty]
        public string Time { get; set; }
    }
}
